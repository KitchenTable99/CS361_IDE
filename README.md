# CS361 IDE created by Caleb Bitting, Matt Cerrato, Erik Cohen, and Ian Ellmer

## Process for changing code
1. Work on your own feature branch and never commit directly to main.
2. Always start your feature branches from the most updated version of the main branch.
3. If you haven't worked on your feature in quite some time, [rebase](https://www.javatpoint.com/git-rebase#:~:text=Rebasing%20is%20a%20process%20to,a%20linear%20process%20of%20merging.) your branch. 
4. Make narrow commits. If you're working on implementing a particular feature, and you notice a bug. Don't address it in the same commit. Mark it with a TODO comment.
5. Commit often. If you need to roll changes back, it's nice to have a recent commit as opposed to re-writing 100 lines of code just to undo a couple of changes.
6. [Squash](https://www.baeldung.com/ops/git-squash-commits) your commits before making a pull request.
7. Once you've fully implemented the feature you've worked on, submit a pull request detailing the feature and any bugs it introduced.
8. Unless we're on a time crunch, wait for somebody else to review your pull request before merging it into main.