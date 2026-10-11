import { cn } from '@/lib/utils'

function getScoreColor(score) {
  if (score >= 80) return 'text-green-600'
  if (score >= 60) return 'text-yellow-600'
  return 'text-red-500'
}

function getBarColor(score) {
  if (score >= 80) return 'bg-green-500'
  if (score >= 60) return 'bg-yellow-500'
  return 'bg-red-400'
}

export function MatchScore({ score, size = 'md', showBar = false }) {
  const textSize = size === 'lg' ? 'text-3xl' : size === 'sm' ? 'text-sm' : 'text-xl'

  return (
    <div className="flex flex-col gap-1">
      <span className={cn('font-bold', textSize, getScoreColor(score))}>
        {score}% Match
      </span>
      {showBar && (
        <div className="h-1.5 w-full bg-gray-100 rounded-full overflow-hidden">
          <div
            className={cn('h-full rounded-full transition-all', getBarColor(score))}
            style={{ width: `${score}%` }}
          />
        </div>
      )}
    </div>
  )
}
