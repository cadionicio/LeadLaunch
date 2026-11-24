package com.leadlaunch.navigation

object Routes {
    const val S01_SPLASH = "s01_splash"
    const val S02_LOGIN = "s02_login"
    const val S03_REGISTER = "s03_register"
    const val S04_BUSINESS = "s04_business"
    const val S05_HOME = "s05_home"
    const val S06_CATALOG = "s06_catalog"
    const val S07_PRODUCT = "s07_product"
    const val S08_RULES = "s08_rules"
    const val S09_RADAR = "s09_radar"
    const val S10_FINDINGS = "s10_findings"
    const val S11_CAMPAIGNS = "s11_campaigns"
    const val S12_CAMPAIGN_RESULT = "s12_campaign_result"
    const val S13_CREATE_QUOTE = "s13_create_quote"
    const val S14_QUOTE_PREVIEW = "s14_quote_preview"
    const val S15_QUOTES = "s15_quotes"
    const val S16_ANALYTICS = "s16_analytics"
    const val S17_MORE = "s17_more"

    val bottomBarRoutes = setOf(S05_HOME, S06_CATALOG, S15_QUOTES, S11_CAMPAIGNS, S17_MORE)
}
