import TiptapTaskList from '@tiptap/extension-task-list'
import { markRaw } from 'vue'
import type { CommandMenuItemType } from '../../types'
import MdiCheckboxMarkedOutline from '~icons/mdi/checkbox-marked-outline'

const TaskList = TiptapTaskList.extend({
  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems(): CommandMenuItemType[] {
        return [
          {
            priority: 70,
            icon: markRaw(MdiCheckboxMarkedOutline),
            title: '任务列表',
            keywords: ['task', 'todo', '任务', '待办', '清单'],
            command: ({ editor, range }) => {
              editor.chain().focus().deleteRange(range).toggleTaskList().run()
            },
          },
        ]
      },
      getToolboxItems({ editor }) {
        return {
          priority: 45,
          icon: markRaw(MdiCheckboxMarkedOutline),
          title: '任务列表',
          description: '插入任务列表',
          action: () => {
            editor.chain().focus().toggleTaskList().run()
          },
        }
      },
    }
  },
})

export default TaskList