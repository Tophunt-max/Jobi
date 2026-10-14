import Link from 'next/link';

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center p-8">
      <h2 className="text-2xl font-bold text-slate-800">Page Not Found (404)</h2>
      <p className="text-sm text-slate-500 mt-2">The requested admin route does not exist.</p>
      <Link href="/" className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-xs font-semibold hover:bg-blue-700">
        Return to Dashboard
      </Link>
    </div>
  );
}
