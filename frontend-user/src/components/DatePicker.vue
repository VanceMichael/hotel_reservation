<template>
  <div class="date-picker" :class="{ focused: showCalendar }" @click="showCalendar = true">
    <div class="date-picker-icon">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
        <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
        <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
    </div>
    <div class="date-picker-content">
      <label class="date-picker-label">{{ label }}</label>
      <span class="date-picker-display">{{ displayValue }}</span>
    </div>
    
    <Teleport to="body">
      <div v-if="showCalendar" class="calendar-overlay" @click.self="showCalendar = false">
        <div class="calendar-popup">
          <div class="calendar-header">
            <button type="button" class="calendar-nav" @click="prevMonth">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M15 18l-6-6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
            <span class="calendar-title">{{ currentYear }}年{{ currentMonth + 1 }}月</span>
            <button type="button" class="calendar-nav" @click="nextMonth">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <div class="calendar-weekdays">
            <span v-for="day in weekdays" :key="day">{{ day }}</span>
          </div>
          <div class="calendar-days">
            <button
              v-for="(day, index) in calendarDays"
              :key="index"
              type="button"
              class="calendar-day"
              :class="{
                'other-month': !day.currentMonth,
                'today': day.isToday,
                'selected': day.isSelected,
                'disabled': day.isDisabled
              }"
              :disabled="day.isDisabled"
              @click="selectDate(day)"
            >
              {{ day.date }}
            </button>
          </div>
          <div class="calendar-footer">
            <button type="button" class="calendar-btn calendar-btn-ghost" @click="showCalendar = false">取消</button>
            <button type="button" class="calendar-btn calendar-btn-primary" @click="confirmDate">确定</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: String,
  label: String,
  min: String,
  required: Boolean
})

const emit = defineEmits(['update:modelValue'])

const showCalendar = ref(false)
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const tempSelected = ref(null)

// 获取今天的日期字符串（本地时区）
const getTodayString = () => {
  const now = new Date()
  return formatDateToString(now)
}

// 将 Date 对象格式化为 YYYY-MM-DD 字符串（本地时区，避免时区偏移）
const formatDateToString = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const todayString = getTodayString()
const today = new Date()
today.setHours(0, 0, 0, 0)

const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth())

// 初始化时设置当前月份为选中日期或今天
watch(() => props.modelValue, (val) => {
  if (val) {
    const parts = val.split('-')
    currentYear.value = parseInt(parts[0])
    currentMonth.value = parseInt(parts[1]) - 1
  }
}, { immediate: true })

watch(showCalendar, (val) => {
  if (val) {
    // 打开日历时，如果有已选值则使用，否则默认选中今天（如果今天可选）
    if (props.modelValue) {
      tempSelected.value = props.modelValue
      const parts = props.modelValue.split('-')
      currentYear.value = parseInt(parts[0])
      currentMonth.value = parseInt(parts[1]) - 1
    } else {
      // 默认选中今天（如果今天不早于 min）
      const minStr = props.min || ''
      if (!minStr || todayString >= minStr) {
        tempSelected.value = todayString
      } else {
        tempSelected.value = minStr
      }
    }
  }
})

const displayValue = computed(() => {
  if (!props.modelValue) return '请选择日期'
  const parts = props.modelValue.split('-')
  const year = parseInt(parts[0])
  const month = parseInt(parts[1])
  const day = parseInt(parts[2])
  const date = new Date(year, month - 1, day)
  const weekdayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekday = weekdayNames[date.getDay()]
  return `${year}年${month}月${day}日 ${weekday}`
})

const calendarDays = computed(() => {
  const days = []
  const firstDay = new Date(currentYear.value, currentMonth.value, 1)
  const lastDay = new Date(currentYear.value, currentMonth.value + 1, 0)
  const startDay = firstDay.getDay()
  
  // 上个月的日期
  const prevMonthLastDay = new Date(currentYear.value, currentMonth.value, 0).getDate()
  for (let i = startDay - 1; i >= 0; i--) {
    const date = prevMonthLastDay - i
    days.push({
      date,
      dateString: '',
      currentMonth: false,
      isToday: false,
      isSelected: false,
      isDisabled: true
    })
  }
  
  // 当前月的日期
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const dateString = formatDateToString(new Date(currentYear.value, currentMonth.value, i))
    const isDisabled = props.min ? dateString < props.min : false
    const isToday = dateString === todayString
    const isSelected = tempSelected.value === dateString
    
    days.push({
      date: i,
      dateString,
      currentMonth: true,
      isToday,
      isSelected,
      isDisabled
    })
  }
  
  // 下个月的日期（补齐6行）
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    days.push({
      date: i,
      dateString: '',
      currentMonth: false,
      isToday: false,
      isSelected: false,
      isDisabled: true
    })
  }
  
  return days
})

const prevMonth = () => {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

const nextMonth = () => {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

const selectDate = (day) => {
  if (day.isDisabled || !day.currentMonth) return
  tempSelected.value = day.dateString
}

const confirmDate = () => {
  if (tempSelected.value) {
    emit('update:modelValue', tempSelected.value)
  }
  showCalendar.value = false
}
</script>

<style lang="scss" scoped>
.date-picker {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  background: #fff;
  border: 1.5px solid var(--gray-200);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
  flex: 1;

  &:hover {
    border-color: var(--gray-300);
  }

  &.focused {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(15, 23, 42, 0.06);
  }
}

.date-picker-icon {
  color: var(--accent);
  flex-shrink: 0;
}

.date-picker-content {
  flex: 1;
  min-width: 0;
}

.date-picker-label {
  display: block;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--gray-400);
  margin-bottom: 4px;
}

.date-picker-display {
  display: block;
  font-size: 15px;
  font-weight: 500;
  color: var(--gray-900);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.calendar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.calendar-popup {
  background: #fff;
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  padding: 24px;
  min-width: 320px;
  animation: slideUp 0.25s ease-out;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.calendar-nav {
  width: 36px;
  height: 36px;
  border: none;
  background: var(--gray-50);
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gray-600);
  transition: all var(--transition);

  &:hover {
    background: var(--gray-100);
    color: var(--primary);
  }
}

.calendar-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--gray-900);
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 8px;

  span {
    text-align: center;
    font-size: 12px;
    font-weight: 600;
    color: var(--gray-400);
    padding: 8px 0;
  }
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-day {
  aspect-ratio: 1;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--gray-700);
  transition: all var(--transition);
  font-family: var(--font-body);

  &:hover:not(.disabled):not(.other-month) {
    background: var(--gray-100);
  }

  &.other-month {
    color: var(--gray-300);
    cursor: default;
  }

  &.today {
    background: var(--gray-100);
    color: var(--primary);
    font-weight: 700;
  }

  &.selected {
    background: var(--primary);
    color: #fff;

    &:hover {
      background: var(--primary-light);
    }
  }

  &.disabled {
    color: var(--gray-300);
    cursor: not-allowed;
    
    &:hover {
      background: transparent;
    }
  }
}

.calendar-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--gray-100);
}

.calendar-btn {
  padding: 10px 20px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-body);
  cursor: pointer;
  transition: all var(--transition);
  border: none;

  &-ghost {
    background: transparent;
    color: var(--gray-500);

    &:hover {
      background: var(--gray-100);
      color: var(--gray-700);
    }
  }

  &-primary {
    background: var(--primary);
    color: #fff;

    &:hover {
      background: var(--primary-light);
    }
  }
}
</style>
