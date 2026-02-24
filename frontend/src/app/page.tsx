'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { Sparkles, ChevronRight, Tag } from 'lucide-react';
import ProductCard from '@/components/ProductCard';
import { SkeletonCard } from '@/components/LoadingSpinner';
import { ToastContainer, useToast } from '@/components/Toast';

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  active: boolean;
}

interface Category {
  id: number;
  name: string;
  description: string;
}

export default function Home() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const { toasts, showToast, removeToast } = useToast();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [prodRes, catRes] = await Promise.all([
        fetch('/api/products'),
        fetch('/api/categories'),
      ]);
      if (prodRes.ok) setProducts(await prodRes.json());
      if (catRes.ok) setCategories(await catRes.json());
    } catch {
      // backend may not be running — show empty state
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const filtered = selectedCategory
    ? products.filter((p) => p.active) // backend filter would need category on product
    : products;

  return (
    <>
      <ToastContainer toasts={toasts} onRemove={removeToast} />

      {/* ── Hero ─────────────────────────────────────────────── */}
      <section className="relative overflow-hidden px-4 pt-24 pb-20 text-center">
        {/* Glow blobs */}
        <div className="absolute top-0 left-1/4 w-96 h-96 rounded-full blur-3xl opacity-10 pointer-events-none"
          style={{ background: 'radial-gradient(circle, #7c3aed, transparent)' }} />
        <div className="absolute top-10 right-1/4 w-72 h-72 rounded-full blur-3xl opacity-10 pointer-events-none"
          style={{ background: 'radial-gradient(circle, #db2777, transparent)' }} />

        <div className="relative max-w-3xl mx-auto animate-fade-in">
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border border-brand-500/30 bg-brand-900/20 text-brand-300 text-xs font-medium mb-6">
            <Sparkles size={12} />
            New arrivals every week
          </div>
          <h1 className="text-5xl sm:text-6xl font-extrabold text-white leading-tight mb-4">
            Shop the{' '}
            <span className="gradient-text">Future</span>
            <br />of Retail
          </h1>
          <p className="text-lg text-[#9090b0] max-w-xl mx-auto mb-8">
            Discover thousands of premium products curated just for you. Fast delivery, unbeatable prices.
          </p>
          <div className="flex items-center justify-center gap-4 flex-wrap">
            <a href="#products" className="btn-primary flex items-center gap-2">
              Shop Now <ChevronRight size={16} />
            </a>
            <Link href="/auth" className="btn-secondary flex items-center gap-2">
              Create Account
            </Link>
          </div>
        </div>
      </section>

      {/* ── Category Filters ─────────────────────────────────── */}
      {categories.length > 0 && (
        <section className="px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto mb-8">
          <div className="flex items-center gap-2 flex-wrap">
            <Tag size={14} className="text-[#9090b0]" />
            <button
              onClick={() => setSelectedCategory(null)}
              className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-200 border ${selectedCategory === null
                  ? 'border-brand-500 text-brand-300 bg-brand-900/30'
                  : 'border-surface-border text-[#9090b0] hover:border-brand-500/50'
                }`}
            >
              All
            </button>
            {categories.map((c) => (
              <button
                key={c.id}
                onClick={() => setSelectedCategory(c.id === selectedCategory ? null : c.id)}
                className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-200 border ${selectedCategory === c.id
                    ? 'border-brand-500 text-brand-300 bg-brand-900/30'
                    : 'border-surface-border text-[#9090b0] hover:border-brand-500/50'
                  }`}
              >
                {c.name}
              </button>
            ))}
          </div>
        </section>
      )}

      {/* ── Products Grid ─────────────────────────────────────── */}
      <section id="products" className="px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto pb-20">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-white">
            {selectedCategory
              ? categories.find(c => c.id === selectedCategory)?.name
              : 'All Products'}
          </h2>
          {!loading && (
            <span className="text-sm text-[#9090b0]">
              {filtered.length} item{filtered.length !== 1 ? 's' : ''}
            </span>
          )}
        </div>

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)}
          </div>
        ) : filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-24 gap-4">
            <div className="w-20 h-20 rounded-full bg-surface-card border border-surface-border flex items-center justify-center">
              <Sparkles size={32} className="text-[#3d3d60]" />
            </div>
            <p className="text-[#9090b0] text-lg">No products available yet</p>
            <p className="text-[#5a5a7a] text-sm">Check back soon or start adding products via the API</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 animate-fade-in">
            {filtered.map((product) => (
              <ProductCard
                key={product.id}
                {...product}
                onToast={showToast}
              />
            ))}
          </div>
        )}
      </section>
    </>
  );
}
