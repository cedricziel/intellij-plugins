package tanvd.grazi.utils

import org.languagetool.rules.IncorrectExample
import org.languagetool.rules.RuleMatch
import tanvd.grazi.grammar.Typo

fun Iterable<Typo>.spellcheckOnly(): Set<Typo> = filter { it.isSpellingTypo }.toSet()
val Typo.isSpellingTypo: Boolean
    get() = info.rule.isDictionaryBasedSpellingRule

val RuleMatch.typoCategory: Typo.Category
    get() = Typo.Category[rule.category.id.toString()]

fun IncorrectExample.toIncorrectHtml() = this.example.replace("marker", "strong")
fun IncorrectExample.toCorrectHtml() = this.example.replace(Regex("<marker.*marker>"),
        if (corrections.isNotEmpty()) "<strong>${corrections.first()}</strong>" else "")