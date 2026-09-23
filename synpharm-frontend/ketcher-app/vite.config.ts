import { defineConfig } from 'vite'
import { fileURLToPath, URL } from 'node:url'

/**
 * Ketcher 的 dist 里残留了一处 CommonJS 探测：
 *   var U5 = typeof window < 'u' ? require('raphael') : void 0
 * 它所在的文件是 ESM，Rollup 的 commonjs 插件不会处理，于是 require 被当成全局标识符：
 * 浏览器下要么报 ReferenceError，要么（若被打成桩）返回 undefined，
 * 而该值最终被当作 Raphael 构造函数使用（new Raphael(...)），
 * 结果就是编辑器初始化直接失败。
 *
 * 这里不采用「桩」方案（返回 undefined 同样会炸），而是把该调用
 * 改写为对全局引用的读取；真正的 Raphael 由 src 侧的 raphael-bridge
 * 在 Ketcher 之前完成注册（ESM 按 import 顺序求值）。
 */
function ketcherRaphaelRequireFix() {
  return {
    name: 'ketcher-raphael-require-fix',
    enforce: 'pre' as const,
    transform(code: string, id: string) {
      if (!id.includes('node_modules')) return null
      if (!/require\(\s*['"]raphael['"]\s*\)/.test(code)) return null

      return {
        code: code.replace(
          /require\(\s*['"]raphael['"]\s*\)/g,
          'globalThis.__synpharmRaphael'
        ),
        map: null
      }
    }
  }
}

/**
 * 分子编辑器子应用的独立构建配置。
 *
 * 刻意与主应用配置完全分离（不走 vite.config.ts）：
 *   - Ketcher 需要 Node 内置模块 polyfill，这份需求只应留在子应用
 *   - 产物直接落到主应用的 public/ketcher/，由主应用按静态资源原样提供
 *   - 主应用的 vite.config.ts / tsconfig.json 因此无需任何改动
 *
 * 构建：npm run build:ketcher（主应用 build 会先自动执行它）
 */
export default defineConfig({
  root: fileURLToPath(new URL('.', import.meta.url)),

  /** 父页面通过 /ketcher/index.html 访问，资源引用需带同前缀 */
  base: '/ketcher/',

  plugins: [ketcherRaphaelRequireFix()],

  build: {
    outDir: fileURLToPath(new URL('../public/ketcher', import.meta.url)),
    emptyOutDir: true,
    target: 'esnext',
    /** Indigo 化学引擎本身就有 13~21 MB，关掉体积告警噪音 */
    chunkSizeWarningLimit: 30000
  },

  /**
   * Ketcher 依赖链会引用 Node 全局量与内置模块。
   * 浏览器下需要这些替代实现，否则运行时报 process is not defined。
   * 对应依赖：events / assert / util（package.json 中已显式声明）
   */
  define: {
    global: 'globalThis',
    'process.env': '{}'
  }
})
