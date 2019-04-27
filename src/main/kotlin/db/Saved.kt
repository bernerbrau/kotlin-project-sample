package net.bernerbits.sample.db

import com.fasterxml.jackson.annotation.JsonUnwrapped

data class Saved<ID,F>(val id: ID, @field:JsonUnwrapped val fields: F)