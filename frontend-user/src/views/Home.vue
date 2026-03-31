<template>
  <div class="home">
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-overlay"></div>
      <div class="container hero-content">
        <span class="hero-badge fade-in-up">探索全球精选酒店</span>
        <h1 class="hero-title fade-in-up" style="animation-delay: 0.1s">
          发现您的<br /><em>完美住所</em>
        </h1>
        <p class="hero-desc fade-in-up" style="animation-delay: 0.2s">
          从城市精品到海滨度假，为您甄选全球优质酒店体验
        </p>
        <router-link to="/hotels" class="btn btn-accent btn-lg fade-in-up" style="animation-delay: 0.3s">
          立即探索
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" style="margin-left: 8px"><path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </router-link>
      </div>
    </section>

    <section class="featured container">
      <div class="section-header fade-in-up">
        <div>
          <span class="section-tag">精选推荐</span>
          <h2>热门酒店</h2>
        </div>
        <router-link to="/hotels" class="btn btn-outline">
          查看全部
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" style="margin-left: 6px"><path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </router-link>
      </div>
      <div class="hotel-grid">
        <div v-for="(hotel, i) in hotels" :key="hotel.id" class="hotel-card fade-in-up" :style="{ animationDelay: `${0.1 + i * 0.08}s` }" @click="$router.push(`/hotels/${hotel.id}`)">
          <div class="hotel-image-wrap">
            <img :src="hotel.coverImage" :alt="hotel.name" class="hotel-image" />
            <div class="hotel-rating">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
              {{ hotel.rating }}
            </div>
          </div>
          <div class="hotel-info">
            <h3>{{ hotel.name }}</h3>
            <p class="hotel-address">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/></svg>
              {{ hotel.address }}
            </p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { hotelApi } from '../api'

const hotels = ref([])

onMounted(async () => {
  const res = await hotelApi.page({ size: 3, status: 1 })
  hotels.value = res.records
})
</script>

<style lang="scss" scoped>
.hero {
  position: relative;
  min-height: 600px;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: url('https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=1920&q=80') center/cover;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.85) 0%, rgba(15, 23, 42, 0.6) 100%);
}

.hero-content {
  position: relative;
  z-index: 1;
  padding: 80px 0;
}

.hero-badge {
  display: inline-block;
  background: rgba(201, 169, 110, 0.15);
  color: var(--accent);
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.05em;
  margin-bottom: 24px;
  border: 1px solid rgba(201, 169, 110, 0.2);
}

.hero-title {
  font-family: var(--font-display);
  font-size: 56px;
  font-weight: 600;
  color: #fff;
  line-height: 1.15;
  margin-bottom: 20px;
  letter-spacing: -0.02em;

  em {
    color: var(--accent);
    font-style: normal;
  }
}

.hero-desc {
  font-size: 17px;
  color: var(--gray-300);
  max-width: 480px;
  line-height: 1.7;
  margin-bottom: 36px;
}

.btn-lg {
  padding: 16px 36px;
  font-size: 15px;
  border-radius: var(--radius-sm);
}

.featured {
  padding: 80px 24px;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 40px;

  h2 {
    font-family: var(--font-display);
    font-size: 32px;
    font-weight: 600;
    color: var(--gray-900);
    margin-top: 8px;
  }
}

.section-tag {
  font-size: 13px;
  font-weight: 600;
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.hotel-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.hotel-card {
  cursor: pointer;
  border-radius: var(--radius);
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--gray-100);
  transition: all var(--transition);

  &:hover {
    transform: translateY(-6px);
    box-shadow: var(--shadow-lg);

    .hotel-image { transform: scale(1.05); }
  }
}

.hotel-image-wrap {
  position: relative;
  overflow: hidden;
  aspect-ratio: 4/3;
}

.hotel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.hotel-rating {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(8px);
  color: var(--accent);
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.hotel-info {
  padding: 20px;

  h3 {
    font-size: 17px;
    font-weight: 600;
    color: var(--gray-900);
    margin-bottom: 8px;
  }
}

.hotel-address {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--gray-500);
  font-size: 14px;

  svg { color: var(--gray-400); flex-shrink: 0; }
}

@media (max-width: 768px) {
  .hotel-grid { grid-template-columns: 1fr; }
  .hero-title { font-size: 36px; }
}
</style>
