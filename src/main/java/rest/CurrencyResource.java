package rest;

import client.ExchangeRateClient;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/currency")
@Produces(MediaType.APPLICATION_JSON)
public class CurrencyResource {

    @EJB
    private ExchangeRateClient exchangeRateClient;

    // GET /api/currency/rates
    @GET
    @Path("/rates")
    public Response getRates() {
        try {
            CurrencyRatesDTO dto = new CurrencyRatesDTO();
            dto.baseCurrency = "USD";
            dto.lastUpdated  = exchangeRateClient.getLastUpdated();
            dto.available    = exchangeRateClient.isAvailable();
            dto.rates        = exchangeRateClient.getRates();
            return Response.ok(dto).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch currency rates\"}").build();
        }
    }

    // GET /api/currency/convert/{currency}?amount=100
    @GET
    @Path("/convert/{currency}")
    public Response convertPrice(
            @PathParam("currency") String currency,
            @QueryParam("amount") double amount) {
        try {
            if (amount <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Amount must be greater than 0\"}").build();
            }
            double converted = exchangeRateClient.convert(amount, currency.toUpperCase());
            double rate      = exchangeRateClient.getRate(currency.toUpperCase());

            ConversionDTO dto = new ConversionDTO();
            dto.originalAmount   = amount;
            dto.baseCurrency     = "USD";
            dto.targetCurrency   = currency.toUpperCase();
            dto.rate             = rate;
            dto.convertedAmount  = Math.round(converted * 100.0) / 100.0;
            dto.available        = exchangeRateClient.isAvailable();
            return Response.ok(dto).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Conversion failed\"}").build();
        }
    }

    // GET /api/currency/refresh  — manually refresh rates
    @GET
    @Path("/refresh")
    public Response refreshRates() {
        try {
            exchangeRateClient.fetchRates();
            return Response.ok("{\"message\":\"Rates refreshed successfully\","
                    + "\"lastUpdated\":\"" + exchangeRateClient.getLastUpdated() + "\"}").build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Refresh failed\"}").build();
        }
    }

    public static class CurrencyRatesDTO {
        public String baseCurrency;
        public String lastUpdated;
        public boolean available;
        public Map<String, Double> rates;
    }

    public static class ConversionDTO {
        public double originalAmount;
        public String baseCurrency;
        public String targetCurrency;
        public double rate;
        public double convertedAmount;
        public boolean available;
    }
}
