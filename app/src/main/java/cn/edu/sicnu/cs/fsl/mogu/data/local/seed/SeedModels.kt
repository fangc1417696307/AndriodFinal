package cn.edu.sicnu.cs.fsl.mogu.data.local.seed

/** assets 下 seed JSON 的反序列化模型。 */

data class SeedCategory(
    val code: String,
    val name: String,
    val sort: Int = 0,
    val remark: String? = null,
)

data class SeedMushroom(
    val mushroomId: Int,
    val name: String,
    val alias: String? = null,
    val categoryCode: String,
    val categoryName: String,
    val edibleType: Int = 3,
    val growthSeason: String? = null,
    val toxicityDesc: String? = null,
    val edibleAdvice: String? = null,
    val content: String? = null,
    val morphology: String? = null,
    val distribution: String? = null,
    val coverImage: String? = null,
    val regions: List<String> = emptyList(),
)

data class SeedRegionsFile(
    val macroNames: Map<String, String> = emptyMap(),
    val provinces: List<SeedProvince> = emptyList(),
)

data class SeedProvince(
    val code: String,
    val name: String,
    val macro: String,
    val cities: List<SeedCity> = emptyList(),
)

data class SeedCity(
    val code: String,
    val name: String,
)
