import type { Metadata } from 'next';
import './globals.css';
import { Sidebar } from '../components/Sidebar';
import { AuthGate } from '../components/AuthGate';

export const metadata: Metadata = {
  title: 'Sarkari Portal Admin',
  description: 'Review and publish official government job notices',
  robots: { index: false, follow: false },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-slate-50 antialiased font-sans">
        <AuthGate>
          <div className="flex">
            <Sidebar />
            <div className="flex-1 ml-64 min-h-screen flex flex-col">
              {children}
            </div>
          </div>
        </AuthGate>
      </body>
    </html>
  );
}
