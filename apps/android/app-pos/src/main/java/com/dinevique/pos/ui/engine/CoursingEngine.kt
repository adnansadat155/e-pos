package com.dinevique.pos.ui.engine

import com.dinevique.pos.ui.models.CourseHoldState
import com.dinevique.pos.ui.models.DraftOrderItem
import java.util.UUID

/**
 * Dining Coursing Engine & Course Hold/Fire Pipeline
 * Source: docs/SOURCES/SYSTEM_PRD.md §5.1 & REQ-01-POS FR-POS-003
 */

enum class CourseCategory(val displayName: String, val sortOrder: Int) {
    BEVERAGE("Beverages", 1),
    APPETIZER("Appetizers", 2),
    ENTREE("Entrees", 3),
    DESSERT("Desserts", 4)
}

data class CourseFiredEventPayload(
    val eventId: String = UUID.randomUUID().toString(),
    val eventType: String = "COURSE_FIRED",
    val orderUuid: String,
    val tableLabel: String,
    val firedCourse: CourseCategory,
    val firedItems: List<DraftOrderItem>,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

object CoursingEngine {

    /**
     * Determines the course category of an order item based on its category ID or prep station
     */
    fun mapItemToCourse(item: DraftOrderItem): CourseCategory {
        return when (item.menuItem.categoryId) {
            "cat_drinks" -> CourseCategory.BEVERAGE
            "cat_apps" -> CourseCategory.APPETIZER
            "cat_mains", "cat_pasta" -> CourseCategory.ENTREE
            "cat_desserts" -> CourseCategory.DESSERT
            else -> CourseCategory.ENTREE
        }
    }

    /**
     * Filters items that are currently ready to be fired (courseHold == FIRE)
     */
    fun extractFiredItems(items: List<DraftOrderItem>): List<DraftOrderItem> {
        return items.filter { it.courseHold == CourseHoldState.FIRE }
    }

    /**
     * Filters items that remain on hold (courseHold == HOLD)
     */
    fun extractHeldItems(items: List<DraftOrderItem>): List<DraftOrderItem> {
        return items.filter { it.courseHold == CourseHoldState.HOLD }
    }

    /**
     * Creates a COURSE_FIRED event payload when a server releases a held course
     */
    fun fireCourse(
        orderUuid: String,
        tableLabel: String,
        course: CourseCategory,
        allItems: List<DraftOrderItem>
    ): Pair<List<DraftOrderItem>, CourseFiredEventPayload?> {
        val courseItems = allItems.filter { mapItemToCourse(it) == course && it.courseHold == CourseHoldState.HOLD }
        if (courseItems.isEmpty()) {
            return allItems to null
        }

        // Update courseHold to FIRE for these items
        val updatedItems = allItems.map { item ->
            if (mapItemToCourse(item) == course && item.courseHold == CourseHoldState.HOLD) {
                item.copy(courseHold = CourseHoldState.FIRE)
            } else {
                item
            }
        }

        val firedList = updatedItems.filter { mapItemToCourse(it) == course }
        val payload = CourseFiredEventPayload(
            orderUuid = orderUuid,
            tableLabel = tableLabel,
            firedCourse = course,
            firedItems = firedList
        )

        return updatedItems to payload
    }
}
