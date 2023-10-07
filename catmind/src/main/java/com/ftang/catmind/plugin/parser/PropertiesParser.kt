package com.ftang.catmind.plugin.parser

interface PropertiesParser {

    fun parse(props: MutableMap<String, Any?>)
}