export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      {Icon && (
        <div className="w-14 h-14 bg-[#0E5C34]/10 border border-[#0E5C34]/20 rounded-full flex items-center justify-center mb-4">
          <Icon className="w-7 h-7 text-[#0E5C34]" />
        </div>
      )}
      <h3 className="text-base font-semibold text-gray-800 mb-1">{title}</h3>
      {description && (
        <p className="text-sm text-gray-500 max-w-xs">{description}</p>
      )}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}
