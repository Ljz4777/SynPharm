<#
  SynPharm 部署前置检查（Windows / PowerShell 5.1+）

  检查项：
    1. Docker 守护进程是否可用
    2. 各宿主机端口是否真的可以绑定

  为什么需要它：
    Windows 会把一段段端口保留给 Hyper-V / WSL，落在保留段内的端口无法绑定，
    报错为 "bind: An attempt was made to access a socket in a way forbidden by
    its access permissions"。该错误只在容器启动阶段才暴露，
    如果先构建了十几分钟才失败，排查成本很高。这里提前失败。

  查看系统保留端口段：
    netsh interface ipv4 show excludedportrange protocol=tcp

  用法示例：
    powershell -NoProfile -File scripts\preflight.ps1 `
        -Ports 80,7000,9050,13307,6380 `
        -Names frontend,backend,fastapi,mysql,redis

  退出码：0 = 通过；1 = 存在阻塞问题

  ⚠ 本文件含中文，必须以「UTF-8 with BOM」保存。
    PowerShell 5.1 对无 BOM 的 .ps1 会按系统 ANSI（中文 Windows 为 GBK）解码，
    中文会被解成乱码并导致字符串终止符解析失败。
#>
[CmdletBinding()]
param(
    # 端口列表，逗号或空格分隔，例如：80,7000,9050,13307,6380
    # 说明：以 -File 方式调用时参数是字符串（不会自动转成数组），
    #       因此这里按字符串接收，再由脚本内部解析。
    [string]$Ports = '',

    # 与端口一一对应的服务名，仅用于输出可读性，例如：frontend,backend
    [string]$Names = ''
)

$script:failed = $false

# ---- 解析入参（兼容 "80,7000" 与 "80 7000" 两种写法）----
$portList = @()
foreach ($token in ($Ports -split '[,\s]+')) {
    if ($token -match '^\d+$') { $portList += [int]$token }
}

$nameList = @()
foreach ($token in ($Names -split '[,\s]+')) {
    if ($token) { $nameList += $token }
}

function Write-Check {
    param(
        [string]$Level,
        [string]$Message,
        [string]$Hint
    )
    $color = switch ($Level) { 'OK' { 'Green' } 'WARN' { 'Yellow' } 'ERROR' { 'Red' } default { 'Gray' } }
    Write-Host ("  [{0,-5}] {1}" -f $Level, $Message) -ForegroundColor $color
    if ($Hint) {
        Write-Host ("            -> {0}" -f $Hint) -ForegroundColor DarkGray
    }
}

# ---------------------------------------------------------------------------
# 1. Docker 守护进程
# ---------------------------------------------------------------------------
Write-Host "Docker 守护进程"

$dockerOk = $false
$dockerVersion = ''
try {
    $out = docker info --format '{{.ServerVersion}}' 2>$null
    if ($LASTEXITCODE -eq 0 -and $out) {
        $dockerOk = $true
        $dockerVersion = (($out | Select-Object -First 1) -as [string]).Trim()
    }
} catch {
    $dockerOk = $false
}

if ($dockerOk) {
    Write-Check 'OK' "可用（Engine $dockerVersion）"
} else {
    Write-Check 'ERROR' '不可用' '请先启动 Docker Desktop，等状态显示 Engine running 后再运行本脚本。'
    $script:failed = $true
}

# ---------------------------------------------------------------------------
# 2. 宿主机端口可绑定性
# ---------------------------------------------------------------------------
if ($portList.Count -gt 0) {
    Write-Host ''
    Write-Host '宿主机端口'

    # 收集 Windows/Hyper-V 保留端口段
    $reserved = @()
    $netshOutput = netsh interface ipv4 show excludedportrange protocol=tcp 2>$null
    foreach ($line in @($netshOutput)) {
        if ($line -match '^\s*(\d+)\s+(\d+)') {
            $reserved += [pscustomobject]@{ Start = [int]$Matches[1]; End = [int]$Matches[2] }
        }
    }

    for ($i = 0; $i -lt $portList.Count; $i++) {
        $port = $portList[$i]
        $name = if ($i -lt $nameList.Count) { $nameList[$i] } else { "port-$port" }

        # 当前正在监听该端口的进程（可能为 $null）
        $owner = $null
        $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
            Select-Object -First 1
        if ($conn) {
            $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
            $owner = if ($proc) { $proc.ProcessName } else { "PID $($conn.OwningProcess)" }
        }

        # 尝试独占绑定。Windows 下必须显式设置 ExclusiveAddressUse（等价 SO_EXCLUSIVEADDRUSE）：
        #   1) 端口若落在系统保留段，此处会抛"以一种访问权限不允许的方式…"，正是容器启动时的报错；
        #   2) 若不设置该属性，默认的 SO_REUSEADDR 语义会让已被占用的端口也绑定成功，检测形同虚设。
        $bindable = $false
        $bindError = ''
        try {
            $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any, $port)
            $listener.ExclusiveAddressUse = $true
            $listener.Start()
            $listener.Stop()
            $bindable = $true
        } catch {
            $bindable = $false
            $bindError = $_.Exception.Message
        }

        if ($bindable) {
            if ($owner) {
                # 能绑定但已有监听者：Docker Desktop 的端口转发（wslrelay）属于这种情形
                if ($owner -match 'docker|vpnkit|wslrelay') {
                    Write-Check 'OK' `
                        ("{0,-10} {1} 已由 Docker 转发占用（{2}，本项目容器在运行，正常）" -f $name, $port, $owner)
                } else {
                    Write-Check 'WARN' `
                        ("{0,-10} {1} 已有进程在监听（{2}）" -f $name, $port, $owner) `
                        '若为本项目容器或已知服务可忽略；否则请先停止它，或更换 deploy\.env 中的 *_PORT。'
                }
            } else {
                Write-Check 'OK' ("{0,-10} {1} 可绑定" -f $name, $port)
            }
            continue
        }

        $hit = $reserved |
            Where-Object { $port -ge $_.Start -and $port -le $_.End } |
            Select-Object -First 1

        if ($hit) {
            Write-Check 'ERROR' `
                ("{0,-10} {1} 落在 Windows/Hyper-V 保留端口段 {2}-{3}，无法绑定" -f $name, $port, $hit.Start, $hit.End) `
                ("请修改 deploy\.env 中对应的 *_PORT，改用保留段之外的端口（例如 {0}）" -f ($hit.End + 10000))
        } elseif ($owner) {
            Write-Check 'ERROR' `
                ("{0,-10} {1} 已被进程 '{2}' 占用" -f $name, $port, $owner) `
                '请结束该进程，或修改 deploy\.env 中对应的 *_PORT。'
        } else {
            Write-Check 'ERROR' `
                ("{0,-10} {1} 无法绑定（{2}）" -f $name, $port, $bindError) `
                '请检查防火墙 / 安全软件，或改用其他端口。'
        }

        $script:failed = $true
    }
}

Write-Host ''
if ($script:failed) {
    Write-Host '前置检查未通过。' -ForegroundColor Red
    exit 1
}

Write-Host '前置检查全部通过。' -ForegroundColor Green
exit 0
