package io.github.khaledbahaaeldin.emberbyte.tokens

/**
 * Files that still contain raw corner radii or raw text-size literals, with how many sites each has today.
 *
 * T12b (the migration sweep) moves every site onto the shape scale and the typography roles and shrinks these maps
 * until they are empty. The scan tests fail when a file has MORE sites than listed (a new raw radius or size was
 * added) and when a listed file no longer exists. Never add an entry to make a test pass: use the tokens instead.
 * Sizes count `12.sp`, `14.5f.sp` and `TextUnit(` constructions.
 */
internal object TokenAllowLists {
    val rawRadii: Map<String, Int> = emptyMap()

    val rawSizes: Map<String, Int> = emptyMap()
}
