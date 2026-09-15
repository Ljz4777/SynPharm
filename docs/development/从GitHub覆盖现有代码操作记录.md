# 从 GitHub 覆盖现有代码操作记录

## 1. 适用场景

当本地项目需要使用另一个 GitHub 仓库的最新代码覆盖当前代码，同时希望保留本地原有改动的备份时，使用本文档中的流程。

本文记录的目标仓库为：

```text
https://github.com/laohuang54/SynPharm.git
```

当前项目目录为：

```text
D:\SynPharm
```

## 2. 操作原则

1. 覆盖前先检查 Git 状态。
2. 使用 `git stash -u` 备份已修改文件和未跟踪文件。
3. 先拉取目标仓库，再确认目标提交。
4. 只在确认备份成功后执行覆盖。
5. 覆盖后验证当前提交与目标提交一致。
6. 不自动执行 `git push`，避免把目标代码误推送到原远程仓库。

## 3. 标准操作步骤

### 3.1 进入项目并检查状态

```powershell
cd D:\SynPharm
git status --short --branch
git remote -v
git branch --all --verbose --no-abbrev
```

重点确认：

- 当前所在分支。
- 是否存在未提交修改。
- 当前 `origin` 指向哪个仓库。
- 是否已经存在同名远程分支。

### 3.2 备份本地改动

```powershell
git stash push -u -m "backup-before-sync-laohuang54"
git stash list
git status --short
```

`-u` 会同时备份未跟踪文件。确认 `git status --short` 没有需要保留的代码改动后，才继续下一步。

如需查看备份内容：

```powershell
git stash show --stat --oneline 'stash@{0}'
```

### 3.3 添加目标远程仓库

```powershell
git remote add latest https://github.com/laohuang54/SynPharm.git
git remote -v
```

如果提示 `remote latest already exists`，改用：

```powershell
git remote set-url latest https://github.com/laohuang54/SynPharm.git
```

### 3.4 拉取目标仓库

```powershell
git fetch latest
git branch -r
```

确认目标仓库存在 `latest/main`。如果出现网络错误，例如 `Recv failure: Connection was reset`，停止操作并先处理网络或代理问题，不要执行覆盖命令。

### 3.5 确认目标提交

```powershell
git log -1 --oneline --decorate latest/main
git show -s --format="%H%n%ad%n%s" --date=iso-strict latest/main
git diff --stat HEAD..latest/main
```

确认目标分支和提交无误后再继续。目标仓库可能同时存在 `dev`、`release` 或临时分支，不能仅凭分支名称猜测版本。

### 3.6 覆盖当前已跟踪代码

```powershell
git reset --hard latest/main
```

该命令会：

- 将当前分支指向 `latest/main` 的提交。
- 覆盖已跟踪文件。
- 删除目标提交中不存在的已跟踪文件。

该命令不会删除未跟踪文件。执行前必须确认本地改动已经通过 stash 或其他方式备份。

### 3.7 验证覆盖结果

```powershell
git status --short --branch
git rev-parse HEAD
git rev-parse latest/main
git diff --stat HEAD latest/main
git diff --name-status HEAD latest/main
git stash list
```

预期结果：

- `HEAD` 与 `latest/main` 输出相同的提交哈希。
- `git diff --stat HEAD latest/main` 没有输出。
- 备份仍存在于 `stash@{0}`。
- 不应自动执行 `git push`。

## 4. 本次实际执行结果

本次使用的目标提交为：

```text
45afc78689fc4dc7928a0f928c303cef4983a14e
```

提交说明：

```text
Merge branch 'main' of https://github.com/laohuang54/SynPharm
```

实际执行的关键命令：

```powershell
git stash push -u -m "backup-before-sync-laohuang54"
git remote add latest https://github.com/laohuang54/SynPharm.git
git fetch latest
git reset --hard latest/main
```

验证结果：

- 当前 `main` 已指向 `45afc78689fc4dc7928a0f928c303cef4983a14e`。
- 当前代码树与 `latest/main` 一致。
- 原有本地改动仍保存在 `stash@{0}`。
- 没有执行 `git push`。
- 原远程 `origin` 仍保持原配置。

## 5. 恢复覆盖前的本地改动

如果确认需要把覆盖前的本地改动重新应用到当前代码上，先查看备份：

```powershell
git stash list
git stash show --stat --oneline 'stash@{0}'
```

应用备份但保留 stash：

```powershell
git stash apply 'stash@{0}'
```

应用并删除该 stash：

```powershell
git stash pop 'stash@{0}'
```

目标代码与本地改动可能存在冲突。出现冲突时，先执行：

```powershell
git status
```

逐个解决冲突后，再执行构建和测试。没有确认前，不要使用 `git stash drop`。

## 6. 回滚覆盖操作

如果只需要回到覆盖前的提交，可使用覆盖前的提交哈希：

```powershell
git reflog --oneline
git reset --hard <覆盖前的提交哈希>
```

如果还需要恢复覆盖前的本地文件改动：

```powershell
git stash apply 'stash@{0}'
```

执行回滚前应再次确认提交哈希，避免回滚到错误版本。

## 7. 注意事项

- `git reset --hard` 只适合在本地改动已有备份时使用。
- 不要把 `git fetch`、`git reset --hard` 和 `git push` 当作一个不可检查的连续命令执行。
- 覆盖后当前分支可能与原 `origin/main` 分叉，这是正常现象；此时不要直接推送。
- 未跟踪文件不会被 `git reset --hard` 删除，需要单独检查：

```powershell
git status --short
git clean -nd
```

- `git clean -fd` 会删除未跟踪文件，执行前必须确认预览结果，且应确保重要文件已经备份。