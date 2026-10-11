import { Badge } from '@/components/ui/badge'

export function SkillBadge({ skill, variant = 'secondary' }) {
  return (
    <Badge variant={variant} className="text-xs font-normal">
      {skill}
    </Badge>
  )
}
