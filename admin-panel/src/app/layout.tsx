import type { Metadata } from 'next';
import './globals.css';
import { Sidebar } from '../components/Sidebar';

export const metadata: Metadata = {
  title: 'Sarkari Portal Admin | Cloudflare Workers Importer',
  description: 'Automated Government Job Importer & Review Dashboard on Cloudflare Workers & D1',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-slate-50 antialiased font-sans">
        <div className="flex">
          <Sidebar />
          <div className="flex-1 ml-64 min-h-screen flex flex-col">
            {children}
          </div>
        </div>
      </body>
    </html>
  );
}
