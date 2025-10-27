package org.atlas.framework.location;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing countries with their ISO 3166-1 alpha-2 codes and names.
 * This enum provides a standardized way to handle country information across the application.
 */
@Getter
@RequiredArgsConstructor
public enum Country {

    // North America
    US("US", "United States"),
    CA("CA", "Canada"),
    MX("MX", "Mexico"),

    // Europe
    GB("GB", "United Kingdom"),
    DE("DE", "Germany"),
    FR("FR", "France"),
    IT("IT", "Italy"),
    ES("ES", "Spain"),
    NL("NL", "Netherlands"),
    BE("BE", "Belgium"),
    CH("CH", "Switzerland"),
    AT("AT", "Austria"),
    SE("SE", "Sweden"),
    NO("NO", "Norway"),
    DK("DK", "Denmark"),
    FI("FI", "Finland"),
    IE("IE", "Ireland"),
    PT("PT", "Portugal"),
    PL("PL", "Poland"),
    CZ("CZ", "Czech Republic"),
    HU("HU", "Hungary"),
    GR("GR", "Greece"),
    RO("RO", "Romania"),
    BG("BG", "Bulgaria"),
    HR("HR", "Croatia"),
    SI("SI", "Slovenia"),
    SK("SK", "Slovakia"),
    LT("LT", "Lithuania"),
    LV("LV", "Latvia"),
    EE("EE", "Estonia"),
    LU("LU", "Luxembourg"),
    MT("MT", "Malta"),
    CY("CY", "Cyprus"),
    IS("IS", "Iceland"),

    // Asia Pacific
    JP("JP", "Japan"),
    CN("CN", "China"),
    KR("KR", "South Korea"),
    IN("IN", "India"),
    AU("AU", "Australia"),
    NZ("NZ", "New Zealand"),
    SG("SG", "Singapore"),
    HK("HK", "Hong Kong"),
    TW("TW", "Taiwan"),
    TH("TH", "Thailand"),
    MY("MY", "Malaysia"),
    ID("ID", "Indonesia"),
    PH("PH", "Philippines"),
    VN("VN", "Vietnam"),
    BD("BD", "Bangladesh"),
    PK("PK", "Pakistan"),
    LK("LK", "Sri Lanka"),
    MM("MM", "Myanmar"),
    KH("KH", "Cambodia"),
    LA("LA", "Laos"),
    BN("BN", "Brunei"),
    MV("MV", "Maldives"),
    NP("NP", "Nepal"),
    BT("BT", "Bhutan"),
    MN("MN", "Mongolia"),
    KZ("KZ", "Kazakhstan"),
    UZ("UZ", "Uzbekistan"),
    KG("KG", "Kyrgyzstan"),
    TJ("TJ", "Tajikistan"),
    TM("TM", "Turkmenistan"),
    AF("AF", "Afghanistan"),

    // Middle East
    AE("AE", "United Arab Emirates"),
    SA("SA", "Saudi Arabia"),
    QA("QA", "Qatar"),
    KW("KW", "Kuwait"),
    BH("BH", "Bahrain"),
    OM("OM", "Oman"),
    JO("JO", "Jordan"),
    LB("LB", "Lebanon"),
    SY("SY", "Syria"),
    IQ("IQ", "Iraq"),
    IR("IR", "Iran"),
    IL("IL", "Israel"),
    PS("PS", "Palestine"),
    TR("TR", "Turkey"),
    YE("YE", "Yemen"),

    // Africa
    ZA("ZA", "South Africa"),
    EG("EG", "Egypt"),
    NG("NG", "Nigeria"),
    KE("KE", "Kenya"),
    GH("GH", "Ghana"),
    MA("MA", "Morocco"),
    TN("TN", "Tunisia"),
    DZ("DZ", "Algeria"),
    LY("LY", "Libya"),
    SD("SD", "Sudan"),
    ET("ET", "Ethiopia"),
    UG("UG", "Uganda"),
    TZ("TZ", "Tanzania"),
    RW("RW", "Rwanda"),
    ZM("ZM", "Zambia"),
    ZW("ZW", "Zimbabwe"),
    BW("BW", "Botswana"),
    NA("NA", "Namibia"),
    MZ("MZ", "Mozambique"),
    MW("MW", "Malawi"),
    MG("MG", "Madagascar"),
    MU("MU", "Mauritius"),
    SC("SC", "Seychelles"),
    SN("SN", "Senegal"),
    CI("CI", "Côte d'Ivoire"),
    ML("ML", "Mali"),
    BF("BF", "Burkina Faso"),
    NE("NE", "Niger"),
    TD("TD", "Chad"),
    CM("CM", "Cameroon"),
    GA("GA", "Gabon"),
    CG("CG", "Republic of the Congo"),
    CD("CD", "Democratic Republic of the Congo"),
    CF("CF", "Central African Republic"),
    AO("AO", "Angola"),

    // South America
    BR("BR", "Brazil"),
    AR("AR", "Argentina"),
    CL("CL", "Chile"),
    PE("PE", "Peru"),
    CO("CO", "Colombia"),
    VE("VE", "Venezuela"),
    EC("EC", "Ecuador"),
    BO("BO", "Bolivia"),
    PY("PY", "Paraguay"),
    UY("UY", "Uruguay"),
    GY("GY", "Guyana"),
    SR("SR", "Suriname"),
    GF("GF", "French Guiana"),

    // Caribbean
    JM("JM", "Jamaica"),
    CU("CU", "Cuba"),
    DO("DO", "Dominican Republic"),
    HT("HT", "Haiti"),
    TT("TT", "Trinidad and Tobago"),
    BB("BB", "Barbados"),
    BS("BS", "Bahamas"),
    BZ("BZ", "Belize"),
    CR("CR", "Costa Rica"),
    GT("GT", "Guatemala"),
    HN("HN", "Honduras"),
    NI("NI", "Nicaragua"),
    PA("PA", "Panama"),
    SV("SV", "El Salvador");

    /**
     * ISO 3166-1 alpha-2 country code
     */
    private final String code;

    /**
     * Country name in English
     */
    private final String name;

    /**
     * Find a country by its ISO code.
     *
     * @param code the ISO 3166-1 alpha-2 country code
     * @return the Country enum value, or null if not found
     */
    public static Country findByCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (Country country : values()) {
            if (country.code.equalsIgnoreCase(code)) {
                return country;
            }
        }
        return null;
    }

    /**
     * Find a country by its name.
     *
     * @param name the country name
     * @return the Country enum value, or null if not found
     */
    public static Country findByName(String name) {
        if (name == null) {
            return null;
        }
        
        for (Country country : values()) {
            if (country.name.equalsIgnoreCase(name)) {
                return country;
            }
        }
        return null;
    }

    /**
     * Check if a country code is valid.
     *
     * @param code the ISO 3166-1 alpha-2 country code
     * @return true if the code is valid, false otherwise
     */
    public static boolean isValidCode(String code) {
        return findByCode(code) != null;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
