package client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST Client — calls free ExchangeRate API (https://open.er-api.com)
 * to fetch live USD-based currency rates.
 *
 * Used in admin dashboard to display product prices in INR / EUR / GBP.
 *
 * @Singleton + @Startup — fetches rates once on app startup, cached in memory.
 */
@Singleton
@Startup
public class ExchangeRateClient {

    private static final Logger LOG = Logger.getLogger(ExchangeRateClient.class.getName());
    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";

    private Client restClient;

    // Cached rates: currency code → rate vs USD
    private Map<String, Double> rates = new HashMap<>();
    private String lastUpdated = "N/A";
    private boolean available = false;

    @PostConstruct
    public void init() {
        restClient = ClientBuilder.newClient();
        fetchRates();
    }

    /**
     * Calls the external ExchangeRate REST API and caches the result.
     * Called once on startup. Can also be called manually to refresh.
     */
    public void fetchRates() {
        try {
            Response response = restClient
                    .target(API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                String json = response.readEntity(String.class);
                JsonObject root = Json.createReader(
                        new java.io.StringReader(json)).readObject();

                JsonObject ratesObj = root.getJsonObject("rates");

                rates.clear();
                // Store only the currencies relevant to our project
                String[] currencies = {"INR", "EUR", "GBP", "AED", "JPY", "CAD"};
                for (String currency : currencies) {
                    if (ratesObj.containsKey(currency)) {
                        rates.put(currency, ratesObj.getJsonNumber(currency).doubleValue());
                    }
                }

                lastUpdated = root.containsKey("time_last_update_utc")
                        ? root.getString("time_last_update_utc") : "N/A";
                available = true;
                LOG.info("ExchangeRateClient: rates fetched successfully. Last updated: " + lastUpdated);
            } else {
                available = false;
                LOG.warning("ExchangeRateClient: API returned status " + response.getStatus());
            }

        } catch (Exception e) {
            available = false;
            LOG.log(Level.WARNING, "ExchangeRateClient: Failed to fetch rates - " + e.getMessage());
        }
    }

    /**
     * Convert a USD amount to the given currency.
     * Returns the original amount if rates are unavailable.
     */
    public double convert(double usdAmount, String targetCurrency) {
        if (!available || !rates.containsKey(targetCurrency)) {
            return usdAmount;
        }
        return usdAmount * rates.get(targetCurrency);
    }

    /**
     * Get rate for a specific currency vs USD.
     */
    public double getRate(String currency) {
        return rates.getOrDefault(currency, 1.0);
    }

    public Map<String, Double> getRates() { return rates; }
    public String getLastUpdated() { return lastUpdated; }
    public boolean isAvailable() { return available; }

    @PreDestroy
    public void cleanup() {
        if (restClient != null) {
            restClient.close();
        }
    }
}
