build:
	rustc -C target-cpu=native -C opt-level=3 -C debuginfo=0 -C debug-assertions=false -C overflow-checks=false -o factorial_accumulator factorial_accumulator.rs
	rustc -C target-cpu=native -C opt-level=3 -C debuginfo=0 -C debug-assertions=false -C overflow-checks=false -o fibonacci_recursive fibonacci_recursive.rs
	rustc -C target-cpu=native -C opt-level=3 -C debuginfo=0 -C debug-assertions=false -C overflow-checks=false -o sum_range sum_range.rs
	rustc -C target-cpu=native -C opt-level=3 -C debuginfo=0 -C debug-assertions=false -C overflow-checks=false -o iterate_increment iterate_increment.rs
	rustc -C target-cpu=native -C opt-level=3 -C debuginfo=0 -C debug-assertions=false -C overflow-checks=false -o match_options match_options.rs
	rustc -C target-cpu=native -C opt-level=3 -C debuginfo=0 -C debug-assertions=false -C overflow-checks=false -o lookup_tree lookup_tree.rs
	rustc -C target-cpu=native -C opt-level=3 -C debuginfo=0 -C debug-assertions=false -C overflow-checks=false -o erase_unused erase_unused.rs

clean:
	rm -f factorial_accumulator
	rm -f fibonacci_recursive
	rm -f sum_range
	rm -f iterate_increment
	rm -f match_options
	rm -f lookup_tree
	rm -f erase_unused
