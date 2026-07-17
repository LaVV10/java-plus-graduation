package ru.practicum.common;

/**
 * Общие константы проекта, используемые несколькими микросервисами.
 */
public final class Constants {
	/** Единый формат даты-времени во всех сервисах (JSON-представление). */
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/** Имя сервиса статистики (для Feign / @LoadBalanced клиентов по Eureka). */
	public static final String STATS_SERVICE_ID = "stats-server";

	private Constants() {
	}
}
