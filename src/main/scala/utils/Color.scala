package utils

def randomColor() = "#%06x".format(scala.util.Random.nextInt(1 << 24))