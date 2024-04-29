package utils

class Color {
    // generated using https://colorbrewer2.org/#type=qualitative&scheme=Set3&n=12
    val scheme = Array(
        "#a6cee3", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99", "#e31a1c", "#fdbf6f", "#ff7f00", "#cab2d6", "#6a3d9a", "#ffff99", "#b15928",
        "#8dd3c7", "#ffffb3", "#bebada", "#fb8072", "#80b1d3", "#fdb462", "#b3de69", "#fccde5", "#d9d9d9", "#bc80bd", "#ccebc5", "#ffed6f"
    )

    var current = 0

    def nextColor() = {
        current += 1
        scheme(current % scheme.length)
    }
}