# set environment variable KOKA_HOME to the installation directory of koka!
KOKA_HOME=/usr/local/share/koka/v3.1.1
KOKA=koka --target c --stack=2048M --ccopts=-DKK_INT_NOREFCOUNT --sharedir $(KOKA_HOME) -O0
KOKA_VERSION=v3.1.1

build:
	@$(KOKA) -o factorial_accumulator factorial_accumulator.kk && chmod +x factorial_accumulator
	@$(KOKA) -o fibonacci_recursive fibonacci_recursive.kk && chmod +x fibonacci_recursive
	@$(KOKA) -o sum_range sum_range.kk && chmod +x sum_range
	@$(KOKA) -o iterate_increment iterate_increment.kk && chmod +x iterate_increment
	@$(KOKA) -o match_options match_options.kk && chmod +x match_options
	@$(KOKA) -o lookup_tree lookup_tree.kk && chmod +x lookup_tree
	@$(KOKA) -o erase_unused erase_unused.kk && chmod +x erase_unused

clean:
	rm -rf .koka
	rm -f factorial_accumulator
	rm -f fibonacci_recursive
	rm -f sum_range
	rm -f iterate_increment
	rm -f match_options
	rm -f lookup_tree
	rm -f erase_unused
