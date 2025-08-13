import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class conversor {
    @SerializedName("result")
    private String result;

    @SerializedName("base_code")
    private String baseCode;

    @SerializedName("conversion_rates")
    private Map<String, Double> conversionRates;

    // Constructores
    public conversor() {}

    // Getters y Setters
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getBaseCode() {
        return baseCode;
    }

    public void setBaseCode(String baseCode) {
        this.baseCode = baseCode;
    }

    public Map<String, Double> getConversionRates() {
        return conversionRates;
    }

    public void setConversionRates(Map<String, Double> conversionRates) {
        this.conversionRates = conversionRates;
    }

    @Override
    public String toString() {
        return "ExchangeRateResponse{" +
                "result='" + result + '\'' +
                ", baseCode='" + baseCode + '\'' +
                ", conversionRates=" + conversionRates +
                '}';
    }
}