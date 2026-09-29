/**
 * Raphael 桥接模块 —— 必须在 `ketcher-react` 之前被求值。
 * ============================================================================
 * Ketcher 的 dist 中残留一处 CommonJS 探测：
 *   var U5 = typeof window < 'u' ? require('raphael') : void 0
 * 该值最终被当作构造函数使用（`this.paper = new Raphael(...)`），用于结构渲染。
 * 打包成 ESM 后 `require` 不存在，需要把真实模块挂到全局，
 * 再由 ketcher-app/vite.config.ts 中的改写插件把调用点指向这个全局。
 *
 * ESM 按 import 语句顺序求值：main.ts 中本模块先于 ketcher-react 导入，
 * 因此 Ketcher 求值时全局已就绪。
 */
import Raphael from 'raphael'

/**
 * raphael 是无类型声明的 CommonJS 包：module.exports = Raphael。
 * 这里显式放宽类型，避免为第三方包引入额外声明文件。
 */
const globalScope = globalThis as unknown as Record<string, unknown>

globalScope.__synpharmRaphael = Raphael
