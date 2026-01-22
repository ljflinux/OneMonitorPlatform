import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.locale('zh-cn');
dayjs.extend(relativeTime);

/**
 * 格式化日期
 */
export function formatDate(date: string | number | Date, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return '-';
  return dayjs(date).format(format);
}

/**
 * 格式化为相对时间
 */
export function formatRelativeTime(date: string | number | Date): string {
  if (!date) return '-';
  return dayjs(date).fromNow();
}

/**
 * 格式化为日期
 */
export function formatDateOnly(date: string | number | Date): string {
  if (!date) return '-';
  return dayjs(date).format('YYYY-MM-DD');
}

/**
 * 格式化为时间
 */
export function formatTimeOnly(date: string | number | Date): string {
  if (!date) return '-';
  return dayjs(date).format('HH:mm:ss');
}

/**
 * 检查日期是否过期
 */
export function isExpired(date: string | number | Date): boolean {
  if (!date) return true;
  return dayjs(date).isBefore(dayjs());
}

/**
 * 获取日期区间
 */
export function getDateRange(days: number): { start: Date; end: Date } {
  const end = new Date();
  const start = new Date();
  start.setDate(start.getDate() - days);
  return { start, end };
}

export default {
  format: formatDate,
  relative: formatRelativeTime,
  dateOnly: formatDateOnly,
  timeOnly: formatTimeOnly,
  isExpired,
  getDateRange,
};
