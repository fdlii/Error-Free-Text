package ru.bysenla.util;

import java.util.List;

public record SpellError(int code, int pos, int row, int col, int len,
                         String word, List<String> s) {}
