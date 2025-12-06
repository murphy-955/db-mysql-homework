<template>
  <div class="calendar">
    <div class="calendar-header">
      <button @click="prevMonth" class="nav-btn">&lt;</button>
      <span class="current-month">{{ currentMonth }}</span>
      <button @click="nextMonth" class="nav-btn">&gt;</button>
    </div>
    <div class="calendar-grid">
      <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
      <div
        v-for="day in days"
        :key="day.date"
        class="day"
        :class="{
          'is-today': day.isToday,
          'is-selected': day.isSelected,
          'is-other-month': !day.isCurrentMonth
        }"
        @click="selectDate(day)"
      >
        {{ day.day }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CalendarView',
  props: {
    initialDate: {
      type: Date,
      default: () => new Date()
    }
  },
  data() {
    const date = this.initialDate;
    return {
      currentDate: date,
      selectedDate: date
    };
  },
  computed: {
    currentMonth() {
      return this.currentDate.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' });
    },
    weekdays() {
      return ['日', '一', '二', '三', '四', '五', '六'];
    },
    days() {
      const year = this.currentDate.getFullYear();
      const month = this.currentDate.getMonth();
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const firstDayOfMonth = new Date(year, month, 1);
      const lastDayOfMonth = new Date(year, month + 1, 0);
      const startDayOfWeek = firstDayOfMonth.getDay();
      const daysInMonth = lastDayOfMonth.getDate();

      const daysArray = [];

      // 上个月的日期
      const prevMonthLastDay = new Date(year, month, 0).getDate();
      for (let i = startDayOfWeek - 1; i >= 0; i--) {
        const date = new Date(year, month - 1, prevMonthLastDay - i);
        daysArray.push({
          date: date.toISOString().split('T')[0],
          day: prevMonthLastDay - i,
          isCurrentMonth: false
        });
      }

      // 本月的日期
      for (let i = 1; i <= daysInMonth; i++) {
        const date = new Date(year, month, i);
        daysArray.push({
          date: date.toISOString().split('T')[0],
          day: i,
          isCurrentMonth: true,
          isToday: date.getTime() === today.getTime(),
          isSelected: date.getTime() === this.selectedDate.getTime()
        });
      }

      // 下个月的日期
      const remainingDays = 42 - daysArray.length; // 6行 * 7天
      for (let i = 1; i <= remainingDays; i++) {
        const date = new Date(year, month + 1, i);
        daysArray.push({
          date: date.toISOString().split('T')[0],
          day: i,
          isCurrentMonth: false
        });
      }

      return daysArray;
    }
  },
  methods: {
    prevMonth() {
      this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    },
    nextMonth() {
      this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    },
    selectDate(day) {
      if (!day.isCurrentMonth) return;
      this.selectedDate = new Date(day.date);
      this.$emit('date-selected', this.selectedDate);
    }
  },
  watch: {
    initialDate(newDate) {
      this.currentDate = newDate;
      this.selectedDate = newDate;
    }
  }
};
</script>

<style scoped>
.calendar {
  width: 100%;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.current-month {
  font-size: 16px;
  font-weight: 600;
}

.nav-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
  text-align: center;
}

.weekday {
  font-size: 12px;
  color: #999;
  font-weight: 500;
}

.day {
  font-size: 14px;
  padding: 8px 0;
  border-radius: 50%;
  cursor: pointer;
  transition: background-color 0.2s;
}

.day:hover {
  background-color: #f0f0f0;
}

.is-other-month {
  color: #ccc;
  cursor: default;
}
.is-other-month:hover {
  background-color: transparent;
}

.is-today {
  background-color: #e6f7ff;
  color: #1890ff;
}

.is-selected {
  background-color: #1890ff;
  color: #fff;
}
</style>
