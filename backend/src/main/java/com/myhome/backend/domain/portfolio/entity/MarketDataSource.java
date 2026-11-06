package com.myhome.backend.domain.portfolio.entity;

/** Where a price or FX rate came from. MANUAL rows are never overwritten by collectors. */
public enum MarketDataSource {
	KIS, KEXIM, MANUAL
}
