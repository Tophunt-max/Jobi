/** @type {import('next').NextConfig} */
const nextConfig = {
  // Fully static build (out/) for Cloudflare Pages. All data is fetched client-side
  // from the Worker, so no server runtime is needed.
  output: 'export',
  trailingSlash: true,
  images: {
    unoptimized: true,
  },
  poweredByHeader: false,
};

export default nextConfig;
