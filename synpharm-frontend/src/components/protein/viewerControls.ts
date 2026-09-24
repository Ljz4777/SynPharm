/**
 * Mol* 3D 视图的显示控制接口（唯一来源）
 * ============================================================================
 * `Visualization.vue`（预测域 3D 页）与 `PocketView.vue`（工作台口袋页签）
 * 需要同一套「显示模式 / 颜色方案 / Mol* 映射 / 组件句柄」。
 * 此前两边各写一份，改一处必漏另一处 —— 本模块把这份接口收口到一处。
 *
 * 刻意**不统一**的部分：两个页面的「设置项」列表
 * （口袋页签多出「口袋腔体」与「共结晶配体」两项），那是真实的界面差异，
 * 强行合并反而会让两边都别扭。
 */
import { nextTick, ref, type Ref } from 'vue'

/* ============================ 显示模式 ============================ */

export type ViewerDisplayMode = 'cartoon' | 'sphere' | 'stick' | 'surface'

/** 与 `MolstarViewer` 内部的表示类型对应（取值与既有 3D 可视化页保持一致） */
export const MOLSTAR_REP_TYPES: Record<ViewerDisplayMode, string> = {
  cartoon: 'cartoon',
  sphere: 'spacefill',
  stick: 'ball-and-stick',
  surface: 'molecular-surface'
}

export const VIEWER_DISPLAY_MODES: Array<{ value: ViewerDisplayMode; label: string }> = [
  { value: 'cartoon', label: '卡通' },
  { value: 'sphere', label: '球体' },
  { value: 'stick', label: '棍状' },
  { value: 'surface', label: '表面' }
]

/* ============================ 颜色方案 ============================ */

export type ViewerColorScheme = 'chain' | 'element' | 'secondary' | 'uniform'

/** 与 `MolstarViewer` 内部的着色类型对应 */
export const MOLSTAR_COLOR_TYPES: Record<ViewerColorScheme, string> = {
  chain: 'chain-id',
  element: 'element-symbol',
  secondary: 'secondary-structure',
  uniform: 'uniform'
}

export const VIEWER_COLOR_SCHEMES: Array<{
  value: ViewerColorScheme
  label: string
  preview: string
}> = [
  { value: 'chain', label: '链颜色', preview: 'linear-gradient(to right, #1a1a2e, #0f3460)' },
  { value: 'element', label: '元素', preview: 'linear-gradient(to right, #4CAF50, #FF9800, #2196F3)' },
  { value: 'secondary', label: '二级结构', preview: 'linear-gradient(to right, #E91E63, #2196F3)' },
  { value: 'uniform', label: '单色', preview: '#1a1a2e' }
]

/* ============================ 组件句柄 ============================ */

/**
 * `MolstarViewer` 通过 `defineExpose` 暴露的方法子集。
 *
 * 显式声明而不是用 `InstanceType<typeof MolstarViewer>`：工作台用的是异步组件，
 * 拿不到组件类型；且只依赖这几个方法能让两边的耦合面一致、可对照。
 */
export interface MolstarHandle {
  resetView: () => void
  exportImage: () => Promise<void> | void
  updateRepresentation: (type: string) => void
  setColorScheme: (scheme: string) => void
  setGridVisible: (visible: boolean) => void
  setLabelsVisible: (visible: boolean) => void
}

/* ============================ 公共逻辑 ============================ */

/**
 * 显示模式 / 颜色方案的切换，以及「结构加载后重新同步」。
 *
 * 为什么一定要重新同步：Mol* 在加载或重建结构后会回到默认显示（卡通 + 链颜色），
 * 用户在界面上选过的模式与配色必须重新应用一次，否则界面与画面就会不一致。
 *
 * @param viewer 指向 `MolstarViewer` 实例的 ref（异步组件同样适用）
 */
export function useMolstarControls(viewer: Ref<MolstarHandle | null>) {
  const displayMode = ref<ViewerDisplayMode>('cartoon')
  const colorScheme = ref<ViewerColorScheme>('chain')

  function changeDisplayMode(mode: ViewerDisplayMode): void {
    displayMode.value = mode
    nextTick(() => viewer.value?.updateRepresentation(MOLSTAR_REP_TYPES[mode]))
  }

  function changeColorScheme(scheme: ViewerColorScheme): void {
    colorScheme.value = scheme
    nextTick(() => viewer.value?.setColorScheme(MOLSTAR_COLOR_TYPES[scheme]))
  }

  /**
   * 结构加载完成后重新应用当前 UI 状态。
   *
   * @param extra 页面特有的补充同步（如网格、标签），在模式与配色之后调用
   */
  function resyncAfterLoad(extra?: () => void): void {
    nextTick(() => {
      if (displayMode.value !== 'cartoon') {
        viewer.value?.updateRepresentation(MOLSTAR_REP_TYPES[displayMode.value])
      }
      if (colorScheme.value !== 'chain') {
        viewer.value?.setColorScheme(MOLSTAR_COLOR_TYPES[colorScheme.value])
      }
      extra?.()
    })
  }

  return { displayMode, colorScheme, changeDisplayMode, changeColorScheme, resyncAfterLoad }
}
