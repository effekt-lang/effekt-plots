#pragma once

#include <datastructure.h>

int64_t toDouble(int64_t x) { return boxFloat((double)x); }

int64_t toInt(int64_t x) { return (int64_t)unboxFloat(x); }

int64_t parseDouble(int64_t s) { return boxFloat(strtod((char *)s, NULL)); }

int64_t floatAbs(int64_t x) { return boxFloat(fabs(unboxFloat(x))); }

int64_t bitwiseAnd(int64_t x, int64_t y) { return x & y; }

int64_t bitwiseShl(int64_t x, int64_t y) { return x << y; }

int64_t bitwiseXor(int64_t x, int64_t y) { return x ^ y; }
