package de.axelrindle.simplecoins

import io.requery.kotlin.Insertion
import io.requery.kotlin.Update
import io.requery.query.Expression
import io.requery.sql.EntityDataStore

interface Upsertion<E> : Insertion<E>, Update<E> {
    fun <V> with(expression: Expression<V>, value: V): Upsertion<E> {
        set(expression, value)
        value(expression, value)
        return this
    }
}

fun <T> EntityDataStore<T>.upsert(
        clazz: Class<T>,
        key: Int
): Upsertion<*> {
    try {
        findByKey(clazz, key) == null
    } catch (e: NoSuchElementException) {
        return insert(clazz) as Upsertion<*>
    }
    return update(clazz) as Upsertion<*>
}