<template>
  <div class="menu-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><Menu /></el-icon>
          菜单管理
        </h1>
        <p class="page-description">管理系统菜单结构和权限配置</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加菜单
        </el-button>
        <el-button @click="expandAll">
          <el-icon><Expand /></el-icon>
          展开全部
        </el-button>
        <el-button @click="collapseAll">
          <el-icon><Fold /></el-icon>
          收起全部
        </el-button>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-left">
        <el-input
          v-model="searchQuery"
          placeholder="搜索菜单名称或路径"
          style="width: 300px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="filterType"
          placeholder="菜单类型"
          style="width: 120px"
          clearable
          @change="handleFilter"
        >
          <el-option label="目录" value="0" />
          <el-option label="菜单" value="1" />
          <el-option label="按钮" value="2" />
        </el-select>
        
        <el-select
          v-model="filterStatus"
          placeholder="状态"
          style="width: 100px"
          clearable
          @change="handleFilter"
        >
          <el-option label="启用" value="0" />
          <el-option label="禁用" value="1" />
        </el-select>
      </div>
      
      <div class="filter-right">
        <el-button @click="resetFilters">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <!-- 菜单树表格 -->
    <div class="table-container">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="filteredMenus"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="菜单名称" min-width="200">
          <template #default="{ row }">
            <div class="menu-name">
              <el-icon v-if="row.icon" class="menu-icon">
                <component :is="row.icon" />
              </el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag
              :type="getTypeTagType(row.type)"
              size="small"
            >
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="path" label="路由路径" min-width="150">
          <template #default="{ row }">
            <code class="path-code">{{ row.path || '-' }}</code>
          </template>
        </el-table-column>
        
        <el-table-column prop="component" label="组件路径" min-width="180">
          <template #default="{ row }">
            <span class="text-gray-600">{{ row.component || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="permission" label="权限标识" min-width="150">
          <template #default="{ row }">
            <el-tag v-if="row.permission" type="info" size="small">
              {{ row.permission }}
            </el-tag>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="sort" label="排序" width="80" />
        
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="0"
              inactive-value="1"
              @change="updateMenuStatus(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="editMenu(row)"
            >
              编辑
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="addChildMenu(row)"
              v-if="row.type !== '2'"
            >
              添加子菜单
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteMenu(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建/编辑菜单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '创建菜单'"
      width="700px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="menuForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上级菜单" prop="parentId">
              <el-tree-select
                v-model="menuForm.parentId"
                :data="menuTreeOptions"
                :props="{ value: 'id', label: 'name', children: 'children' }"
                placeholder="选择上级菜单"
                check-strictly
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单类型" prop="type">
              <el-radio-group v-model="menuForm.type">
                <el-radio label="0">目录</el-radio>
                <el-radio label="1">菜单</el-radio>
                <el-radio label="2">按钮</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="name">
              <el-input
                v-model="menuForm.name"
                placeholder="请输入菜单名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单图标" v-if="menuForm.type !== '2'">
              <el-input
                v-model="menuForm.icon"
                placeholder="请输入图标名称"
              >
                <template #append>
                  <el-button @click="showIconSelector">选择</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" v-if="menuForm.type !== '0'">
          <el-col :span="12">
            <el-form-item label="路由路径" prop="path">
              <el-input
                v-model="menuForm.path"
                placeholder="请输入路由路径"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="menuForm.type === '1'">
            <el-form-item label="组件路径" prop="component">
              <el-input
                v-model="menuForm.component"
                placeholder="请输入组件路径"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限标识" prop="permission">
              <el-input
                v-model="menuForm.permission"
                placeholder="请输入权限标识"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number
                v-model="menuForm.sort"
                :min="0"
                :max="999"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" v-if="menuForm.type === '1'">
          <el-col :span="12">
            <el-form-item label="是否缓存">
              <el-switch
                v-model="menuForm.keepAlive"
                active-text="是"
                inactive-text="否"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否隐藏">
              <el-switch
                v-model="menuForm.hidden"
                active-text="是"
                inactive-text="否"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="menuForm.status">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="menuForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenu" :loading="saving">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 图标选择器对话框 -->
    <el-dialog
      v-model="iconSelectorVisible"
      title="选择图标"
      width="800px"
    >
      <div class="icon-selector">
        <div class="icon-search">
          <el-input
            v-model="iconSearchQuery"
            placeholder="搜索图标"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="icon-grid">
          <div
            v-for="icon in filteredIcons"
            :key="icon"
            class="icon-item"
            :class="{ active: selectedIcon === icon }"
            @click="selectIcon(icon)"
          >
            <el-icon><component :is="icon" /></el-icon>
            <span class="icon-name">{{ icon }}</span>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="iconSelectorVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmIcon">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>


<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { menuApi } from '@/api/menu'
import type { Menu as SysMenu, MenuForm as SysMenuForm } from '@/types/menu'
import {
  Menu,
  Plus,
  Search,
  Refresh,
  Expand,
  Fold,
  User,
  Setting,
  Document,
  Folder,
  Files,
  DataLine,
  Monitor,
  Key,
  Lock,
  Bell,
  Message,
  Star,
  Flag,
  Tools,
  Grid,
  List,
  Edit,
  Delete,
  View,
  Download,
  Upload,
  Share,
  Link,
  Picture,
  VideoPlay,
  Headset,
  Phone,
  Location,
  Timer,
  Calendar,
  Clock,
  Wallet,
  CreditCard,
  ShoppingCart,
  Goods,
  Box,
  Truck,
  House,
  Office,
  School,
  Hospital,
  Bank,
  Shop,
  Restaurant,
  Hotel,
  Car,
  Bicycle,
  Ship,
  Airplane,
  Train,
  Bus
} from '@element-plus/icons-vue'

interface MenuNode {
  id: string
  menuId?: number
  parentId: string
  name: string
  type: '0' | '1' | '2'
  icon?: string
  path?: string
  component?: string
  permission?: string
  sort: number
  status: string
  keepAlive: boolean
  hidden: boolean
  remark?: string
  children?: MenuNode[]
}

const iconList = [
  'User',
  'Setting',
  'Document',
  'Folder',
  'Files',
  'DataLine',
  'Monitor',
  'Key',
  'Lock',
  'Bell',
  'Message',
  'Star',
  'Flag',
  'Tools',
  'Grid',
  'List',
  'Edit',
  'Delete',
  'View',
  'Download',
  'Upload',
  'Share',
  'Link',
  'Picture',
  'VideoPlay',
  'Headset',
  'Phone',
  'Location',
  'Timer',
  'Calendar',
  'Clock',
  'Wallet',
  'CreditCard',
  'ShoppingCart',
  'Goods',
  'Box',
  'Truck',
  'House',
  'Office',
  'School',
  'Hospital',
  'Bank',
  'Shop',
  'Restaurant',
  'Hotel',
  'Car',
  'Bicycle',
  'Ship',
  'Airplane',
  'Train',
  'Bus'
]

const loading = ref(false)
const saving = ref(false)
const menus = ref<MenuNode[]>([])
const searchQuery = ref('')
const filterType = ref('')
const filterStatus = ref('')
const tableRef = ref()

const dialogVisible = ref(false)
const iconSelectorVisible = ref(false)
const isEdit = ref(false)
const parentMenu = ref<MenuNode | null>(null)
const formRef = ref()

const menuForm = reactive({
  id: '',
  parentId: '',
  name: '',
  type: '1',
  icon: '',
  path: '',
  component: '',
  permission: '',
  sort: 0,
  status: '0',
  keepAlive: true,
  hidden: false,
  remark: ''
})

const iconSearchQuery = ref('')
const selectedIcon = ref('')

const formRules = {
  name: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 20, message: '菜单名称长度需在 2 到 20 个字符之间', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  path: [{ required: true, message: '请输入路由路径', trigger: 'blur' }],
  component: [{ required: true, message: '请输入组件路径', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const filteredMenus = computed(() => {
  let result = cloneTree(menus.value)

  if (searchQuery.value) {
    result = filterMenusBySearch(result, searchQuery.value.toLowerCase())
  }

  if (filterType.value) {
    result = filterMenusByType(result, filterType.value)
  }

  if (filterStatus.value) {
    result = filterMenusByStatus(result, filterStatus.value)
  }

  return result
})

const menuTreeOptions = computed(() => cloneTree(menus.value, (node) => node.type !== '2'))

const filteredIcons = computed(() => {
  if (!iconSearchQuery.value) {
    return iconList
  }
  return iconList.filter((icon) => icon.toLowerCase().includes(iconSearchQuery.value.toLowerCase()))
})

const loadMenus = async () => {
  try {
    loading.value = true
    const data = await menuApi.getMenuList()
    menus.value = normalizeMenuList(Array.isArray(data) ? data : [])
  } catch (error) {
    console.error('加载菜单列表失败', error)
    ElMessage.error('加载菜单列表失败')
  } finally {
    loading.value = false
  }
}

const normalizeMenuList = (items: SysMenu[] = []): MenuNode[] => {
  if (!items.length) {
    return []
  }

  const hasNested = items.some((item) => Array.isArray(item.children) && item.children.length > 0)
  if (hasNested) {
    return items.map((item) => convertNestedMenu(item))
  }

  const nodes = items.map((item) => convertFlatMenu(item))
  return buildTreeFromFlat(nodes)
}

const convertFlatMenu = (menu: SysMenu): MenuNode => ({
  id: menu.menuId ? String(menu.menuId) : generateTempId(),
  menuId: menu.menuId ?? undefined,
  parentId: menu.parentId ? String(menu.parentId) : '',
  name: menu.menuName ?? '',
  type: mapMenuTypeToUi(menu.menuType),
  icon: menu.icon ?? '',
  path: menu.path ?? '',
  component: menu.component ?? '',
  permission: menu.perms ?? '',
  sort: menu.orderNum ?? 0,
  status: menu.status ?? '0',
  keepAlive: (menu.isCache ?? '0') === '0',
  hidden: menu.visible === '1',
  remark: menu.remark ?? '',
  children: []
})

const convertNestedMenu = (menu: SysMenu): MenuNode => {
  const node = convertFlatMenu(menu)
  if (Array.isArray(menu.children) && menu.children.length > 0) {
    node.children = menu.children.map((child) => convertNestedMenu(child))
  }
  return node
}

const buildTreeFromFlat = (nodes: MenuNode[]): MenuNode[] => {
  const map = new Map<string, MenuNode & { children: MenuNode[] }>()
  nodes.forEach((node) => {
    map.set(node.id, { ...node, children: [] })
  })

  const tree: MenuNode[] = []
  map.forEach((node) => {
    if (node.parentId && map.has(node.parentId)) {
      map.get(node.parentId)!.children!.push(node)
    } else {
      tree.push(node)
    }
  })

  const sortTree = (list: MenuNode[]) => {
    list.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    list.forEach((child) => {
      if (child.children?.length) {
        sortTree(child.children)
      }
    })
  }

  sortTree(tree)
  return tree
}

const cloneTree = (nodes: MenuNode[], predicate?: (node: MenuNode) => boolean): MenuNode[] => {
  return nodes
    .filter((node) => (predicate ? predicate(node) : true))
    .map((node) => ({
      ...node,
      children: node.children ? cloneTree(node.children, predicate) : []
    }))
}

const filterMenusBySearch = (nodes: MenuNode[], query: string): MenuNode[] => {
  const result: MenuNode[] = []

  nodes.forEach((node) => {
    const nameMatch = node.name.toLowerCase().includes(query)
    const pathMatch = (node.path ?? '').toLowerCase().includes(query)
    if (nameMatch || pathMatch) {
      result.push({ ...node, children: cloneTree(node.children ?? []) })
    } else if (node.children?.length) {
      const children = filterMenusBySearch(node.children, query)
      if (children.length) {
        result.push({ ...node, children })
      }
    }
  })

  return result
}

const filterMenusByType = (nodes: MenuNode[], type: string): MenuNode[] => {
  const result: MenuNode[] = []

  nodes.forEach((node) => {
    if (node.type === type) {
      result.push({ ...node, children: cloneTree(node.children ?? []) })
    } else if (node.children?.length) {
      const children = filterMenusByType(node.children, type)
      if (children.length) {
        result.push({ ...node, children })
      }
    }
  })

  return result
}

const filterMenusByStatus = (nodes: MenuNode[], status: string): MenuNode[] => {
  const result: MenuNode[] = []

  nodes.forEach((node) => {
    if (node.status === status) {
      result.push({ ...node, children: cloneTree(node.children ?? []) })
    } else if (node.children?.length) {
      const children = filterMenusByStatus(node.children, status)
      if (children.length) {
        result.push({ ...node, children })
      }
    }
  })

  return result
}

const handleSearch = () => {
  if (searchQuery.value) {
    expandAll()
  }
}

const handleFilter = () => {
  if (filterType.value || filterStatus.value) {
    expandAll()
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  filterType.value = ''
  filterStatus.value = ''
}

const expandAll = () => {
  if (tableRef.value?.store) {
    tableRef.value.store.states.defaultExpandAll.value = true
    tableRef.value.store.updateTableScrollY()
  }
}

const collapseAll = () => {
  if (tableRef.value?.store) {
    tableRef.value.store.states.defaultExpandAll.value = false
    tableRef.value.store.updateTableScrollY()
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  parentMenu.value = null
  resetForm()
  dialogVisible.value = true
}

const addChildMenu = (menu: MenuNode) => {
  isEdit.value = false
  parentMenu.value = menu
  resetForm()
  menuForm.parentId = menu.id
  dialogVisible.value = true
}

const editMenu = (menu: MenuNode) => {
  isEdit.value = true
  Object.assign(menuForm, {
    id: menu.id,
    parentId: menu.parentId,
    name: menu.name,
    type: menu.type,
    icon: menu.icon ?? '',
    path: menu.path ?? '',
    component: menu.component ?? '',
    permission: menu.permission ?? '',
    sort: menu.sort ?? 0,
    status: menu.status ?? '0',
    keepAlive: menu.keepAlive,
    hidden: menu.hidden,
    remark: menu.remark ?? ''
  })
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(menuForm, {
    id: '',
    parentId: parentMenu.value?.id || '',
    name: '',
    type: '1',
    icon: '',
    path: '',
    component: '',
    permission: '',
    sort: 0,
    status: '0',
    keepAlive: true,
    hidden: false,
    remark: ''
  })
}

const resolveMenuId = (menu: MenuNode | { id?: string }) => {
  const target = 'menuId' in menu && menu.menuId ? menu.menuId : menu.id
  return target ? Number(target) : 0
}

const createPayloadFromForm = (): SysMenuForm => ({
  menuName: menuForm.name.trim(),
  parentId: menuForm.parentId ? Number(menuForm.parentId) : 0,
  orderNum: Number(menuForm.sort ?? 0),
  path: menuForm.type !== '0' ? (menuForm.path?.trim() || '') : '',
  component: menuForm.type === '1' ? (menuForm.component?.trim() || '') : '',
  query: undefined,
  isFrame: '1',
  isCache: menuForm.type === '1' ? (menuForm.keepAlive ? '0' : '1') : '1',
  menuType: mapMenuTypeToUiInverse(menuForm.type),
  visible: menuForm.hidden ? '1' : '0',
  status: menuForm.status,
  perms: menuForm.permission?.trim() || undefined,
  icon: menuForm.type !== '2' ? menuForm.icon || '' : '',
  remark: menuForm.remark?.trim() || ''
})

const createPayloadFromNode = (menu: MenuNode): SysMenuForm => ({
  menuName: menu.name,
  parentId: menu.parentId ? Number(menu.parentId) : 0,
  orderNum: Number(menu.sort ?? 0),
  path: menu.type !== '0' ? (menu.path ?? '') : '',
  component: menu.type === '1' ? (menu.component ?? '') : '',
  query: undefined,
  isFrame: '1',
  isCache: menu.type === '1' ? (menu.keepAlive ? '0' : '1') : '1',
  menuType: mapMenuTypeToUiInverse(menu.type),
  visible: menu.hidden ? '1' : '0',
  status: menu.status ?? '0',
  perms: menu.permission || undefined,
  icon: menu.type !== '2' ? menu.icon || '' : '',
  remark: menu.remark || ''
})

const mapMenuTypeToUiInverse = (type: string): 'M' | 'C' | 'F' => {
  switch (type) {
    case '0':
      return 'M'
    case '2':
      return 'F'
    default:
      return 'C'
  }
}

const mapMenuTypeToUi = (type?: string): '0' | '1' | '2' => {
  switch (type) {
    case 'M':
      return '0'
    case 'F':
      return '2'
    default:
      return '1'
  }
}

const saveMenu = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    const payload = createPayloadFromForm()

    if (isEdit.value && menuForm.id) {
      await menuApi.updateMenu(Number(menuForm.id), payload)
      ElMessage.success('菜单更新成功')
    } else {
      await menuApi.createMenu(payload)
      ElMessage.success('菜单创建成功')
    }

    dialogVisible.value = false
    await loadMenus()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(isEdit.value ? '菜单更新失败' : '菜单创建失败')
    }
  } finally {
    saving.value = false
  }
}

const updateMenuStatus = async (menu: MenuNode) => {
  try {
    const payload = createPayloadFromNode(menu)
    await menuApi.updateMenu(resolveMenuId(menu), payload)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    menu.status = menu.status === '0' ? '1' : '0'
  }
}

const deleteMenu = async (menu: MenuNode) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜单"${menu.name}" 吗？删除后其子菜单也将被删除！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await menuApi.deleteMenu(resolveMenuId(menu))
    ElMessage.success('菜单删除成功')
    await loadMenus()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('菜单删除失败')
    }
  }
}

const showIconSelector = () => {
  selectedIcon.value = menuForm.icon
  iconSelectorVisible.value = true
}

const selectIcon = (icon: string) => {
  selectedIcon.value = icon
}

const confirmIcon = () => {
  menuForm.icon = selectedIcon.value
  iconSelectorVisible.value = false
}

const generateTempId = () => `temp-${Math.random().toString(36).slice(2, 10)}`

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    '0': '目录',
    '1': '菜单',
    '2': '按钮'
  }
  return map[type] || '未知'
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    '0': '',
    '1': 'success',
    '2': 'warning'
  }
  return map[type] || ''
}

onMounted(() => {
  void loadMenus()
})
</script>


<style scoped>
.menu-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.filter-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-right {
  display: flex;
  gap: 8px;
}

.table-container {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.menu-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-icon {
  color: #606266;
}

.path-code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #e6a23c;
}

.icon-selector {
  padding: 20px 0;
}

.icon-search {
  margin-bottom: 20px;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.icon-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.icon-item.active {
  border-color: #409eff;
  background: #409eff;
  color: white;
}

.icon-name {
  font-size: 12px;
  text-align: center;
  word-break: break-all;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .menu-management {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .filter-toolbar {
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-left,
  .filter-right {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .icon-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }
}
</style>