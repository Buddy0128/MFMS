interface LoadingSpinnerProps {
  fullPage?: boolean;
}

export default function LoadingSpinner({ fullPage }: LoadingSpinnerProps) {
  const spinner = (
    <div className="flex items-center justify-center">
      <div className="w-10 h-10 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin" />
    </div>
  );
  if (fullPage) return <div className="min-h-[50vh] flex items-center justify-center">{spinner}</div>;
  return spinner;
}
