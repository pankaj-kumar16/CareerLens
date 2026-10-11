import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'

const statusStyles = {
  Applied: 'bg-blue-50 text-blue-700 border-blue-200',
  'Under Review': 'bg-yellow-50 text-yellow-700 border-yellow-200',
  Shortlisted: 'bg-purple-50 text-purple-700 border-purple-200',
  Interview: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  Selected: 'bg-green-50 text-green-700 border-green-200',
  Rejected: 'bg-red-50 text-red-700 border-red-200',
  Active: 'bg-green-50 text-green-700 border-green-200',
  Closed: 'bg-gray-100 text-gray-600 border-gray-200',
}

export function StatusBadge({ status }) {
  return (
    <Badge
      variant="outline"
      className={cn('font-medium text-xs rounded-[4px]', statusStyles[status] ?? 'bg-gray-100 text-gray-600')}
    >
      {status}
    </Badge>
  )
}
