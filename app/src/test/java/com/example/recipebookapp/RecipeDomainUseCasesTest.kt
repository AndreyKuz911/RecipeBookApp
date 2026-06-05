package com.example.recipebookapp

import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.core.model.UserSummary
import com.example.recipebookapp.feature_profile.domain.BuildProfileEditorFieldsUseCase
import com.example.recipebookapp.feature_profile.domain.GetEditableMyProfileUseCase
import com.example.recipebookapp.feature_profile.domain.MergeUpdatedProfileUseCase
import com.example.recipebookapp.feature_profile.domain.GetMyProfileWithRecipesUseCase
import com.example.recipebookapp.feature_profile.domain.ProfileRepository
import com.example.recipebookapp.feature_profile.domain.model.ProfileWithRecipes
import com.example.recipebookapp.feature_recipes.domain.ApplyOptimisticFavoriteUseCase
import com.example.recipebookapp.feature_recipes.domain.ApplyOptimisticRatingUseCase
import com.example.recipebookapp.feature_recipes.domain.BuildRecipeDraftUseCase
import com.example.recipebookapp.feature_recipes.domain.GetRecipeCommentsUseCase
import com.example.recipebookapp.feature_recipes.domain.GetRecipeDetailsUseCase
import com.example.recipebookapp.feature_recipes.domain.LoadRecipeDetailsContentUseCase
import com.example.recipebookapp.feature_recipes.domain.PrependCommentUseCase
import com.example.recipebookapp.feature_recipes.domain.RecipeDraft
import com.example.recipebookapp.feature_recipes.domain.RecipesRepository
import com.example.recipebookapp.feature_recipes.domain.model.PagedRecipes
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeDomainUseCasesTest {

    private val author = UserSummary(id = "u1", username = "chef")

    @Test
    fun `apply optimistic rating adds positive rating for unrated recipe`() {
        val useCase = ApplyOptimisticRatingUseCase()
        val details = sampleDetails(myRating = null, likesCount = 3, dislikesCount = 1, rating = 2)

        val updated = useCase(details, 1)

        assertEquals(4, updated.likesCount)
        assertEquals(1, updated.dislikesCount)
        assertEquals(3, updated.rating)
        assertEquals(1, updated.myRating)
    }

    @Test
    fun `apply optimistic rating removes existing positive rating on repeated tap`() {
        val useCase = ApplyOptimisticRatingUseCase()
        val details = sampleDetails(myRating = 1, likesCount = 5, dislikesCount = 2, rating = 3)

        val updated = useCase(details, 1)

        assertEquals(4, updated.likesCount)
        assertEquals(2, updated.dislikesCount)
        assertEquals(2, updated.rating)
        assertNull(updated.myRating)
    }

    @Test
    fun `apply optimistic favorite toggles favorite state`() {
        val useCase = ApplyOptimisticFavoriteUseCase()
        val details = sampleDetails(isFavorite = false)

        val updated = useCase(details)

        assertTrue(updated.isFavorite)
    }

    @Test
    fun `build recipe draft preserves content and fills non empty collections`() {
        val useCase = BuildRecipeDraftUseCase()
        val details = sampleDetails(
            ingredients = emptyList(),
            steps = listOf("Step 1"),
            imageUrls = emptyList(),
        )

        val draft = useCase(details)

        assertEquals(details.title, draft.title)
        assertEquals(listOf(""), draft.ingredients)
        assertEquals(listOf("Step 1"), draft.steps)
        assertEquals(listOf(""), draft.imageUrls)
    }

    @Test
    fun `prepend comment puts new comment first`() {
        val useCase = PrependCommentUseCase()
        val first = sampleComment("c1")
        val second = sampleComment("c2")

        val updated = useCase(listOf(first), second)

        assertEquals(listOf(second, first), updated)
    }

    @Test
    fun `merge updated profile keeps existing recipes`() {
        val useCase = MergeUpdatedProfileUseCase()
        val existing = ProfileWithRecipes(
            profile = sampleProfile("old"),
            recipes = listOf(sampleRecipe("r1")),
        )

        val merged = useCase(existing, sampleProfile("new"))

        assertEquals("new", merged.profile.username)
        assertEquals(1, merged.recipes.size)
        assertEquals("r1", merged.recipes.first().id)
    }

    @Test
    fun `merge updated profile returns empty recipes when current is null`() {
        val useCase = MergeUpdatedProfileUseCase()

        val merged = useCase(null, sampleProfile("solo"))

        assertEquals("solo", merged.profile.username)
        assertTrue(merged.recipes.isEmpty())
    }

    @Test
    fun `build profile editor fields normalizes nullable profile values`() {
        val useCase = BuildProfileEditorFieldsUseCase()

        val fields = useCase(sampleProfile("chef").copy(bio = null, avatarUrl = null))

        assertEquals("chef", fields.username)
        assertEquals("", fields.bio)
        assertEquals("", fields.avatarUrl)
    }

    @Test
    fun `load recipe details content returns details and comments together`() = runTest {
        val repository = object : RecipesRepository {
            override suspend fun getRecipes(filters: RecipeFilters, page: Int, limit: Int) = error("Not used")
            override suspend fun getRecipeDetails(recipeId: String) = com.example.recipebookapp.core.common.Resource.Success(sampleDetails())
            override suspend fun createRecipe(draft: RecipeDraft) = error("Not used")
            override suspend fun updateRecipe(recipeId: String, draft: RecipeDraft) = error("Not used")
            override suspend fun deleteRecipe(recipeId: String) = error("Not used")
            override suspend fun rateRecipe(recipeId: String, value: Int) = error("Not used")
            override suspend fun clearRating(recipeId: String) = error("Not used")
            override suspend fun toggleFavorite(recipeId: String, currentlyFavorite: Boolean) = error("Not used")
            override suspend fun getComments(recipeId: String) = com.example.recipebookapp.core.common.Resource.Success(listOf(sampleComment("c1")))
            override suspend fun addComment(recipeId: String, text: String, parentCommentId: String?) = error("Not used")
        }

        val result = LoadRecipeDetailsContentUseCase(
            getRecipeDetails = GetRecipeDetailsUseCase(repository),
            getRecipeComments = GetRecipeCommentsUseCase(repository),
        )("r1")

        assertTrue(result.details is com.example.recipebookapp.core.common.Resource.Success)
        assertTrue(result.comments is com.example.recipebookapp.core.common.Resource.Success)
        assertEquals(1, (result.comments as com.example.recipebookapp.core.common.Resource.Success).data.size)
    }

    @Test
    fun `get editable my profile returns profile and editor fields`() = runTest {
        val repository = object : ProfileRepository {
            override suspend fun getMyProfileWithRecipes() = com.example.recipebookapp.core.common.Resource.Success(
                ProfileWithRecipes(
                    profile = sampleProfile("chef").copy(bio = null, avatarUrl = null),
                    recipes = listOf(sampleRecipe("r1")),
                ),
            )
            override suspend fun getOtherProfileWithRecipes(userId: String) = error("Not used")
            override suspend fun updateProfile(username: String, bio: String, avatarUrl: String) = error("Not used")
            override suspend fun setFollowing(userId: String, shouldFollow: Boolean) = error("Not used")
        }

        val result = GetEditableMyProfileUseCase(
            getMyProfileWithRecipes = GetMyProfileWithRecipesUseCase(repository),
            buildProfileEditorFields = BuildProfileEditorFieldsUseCase(),
        )()

        assertTrue(result is com.example.recipebookapp.core.common.Resource.Success)
        val data = (result as com.example.recipebookapp.core.common.Resource.Success).data
        assertEquals("chef", data.profile.profile.username)
        assertEquals("", data.editorFields.bio)
        assertEquals("", data.editorFields.avatarUrl)
    }

    private fun sampleDetails(
        myRating: Int? = null,
        likesCount: Int = 0,
        dislikesCount: Int = 0,
        rating: Int = 0,
        isFavorite: Boolean = false,
        ingredients: List<String> = listOf("salt"),
        steps: List<String> = listOf("mix"),
        imageUrls: List<String> = listOf("img"),
    ) = RecipeDetails(
        id = "r1",
        title = "Soup",
        description = "Good soup",
        category = "Dinner",
        cookingTimeMinutes = 15,
        ingredients = ingredients,
        steps = steps,
        imageUrls = imageUrls,
        author = author,
        createdAt = "2026-01-01T00:00:00",
        updatedAt = "2026-01-01T00:00:00",
        likesCount = likesCount,
        dislikesCount = dislikesCount,
        rating = rating,
        isFavorite = isFavorite,
        myRating = myRating,
    )

    private fun sampleComment(id: String) = Comment(
        id = id,
        recipeId = "r1",
        parentCommentId = null,
        text = "comment-$id",
        createdAt = "2026-01-01T00:00:00",
        author = author,
    )

    private fun sampleProfile(username: String) = UserProfile(
        id = "u1",
        email = "chef@example.com",
        username = username,
        bio = "bio",
        avatarUrl = null,
        createdAt = "2026-01-01T00:00:00",
        recipesCount = 2,
        followersCount = 3,
        followingCount = 4,
        isFollowing = false,
    )

    private fun sampleRecipe(id: String) = Recipe(
        id = id,
        title = "Recipe $id",
        description = "desc",
        category = "Dinner",
        cookingTimeMinutes = 10,
        imageUrl = null,
        author = author,
        createdAt = "2026-01-01T00:00:00",
        updatedAt = "2026-01-01T00:00:00",
        likesCount = 0,
        dislikesCount = 0,
        rating = 0,
        isFavorite = false,
        myRating = null,
    )
}
