# OpenGrokRepoUpdater

The OpenGrokRepoUpdater was intended to be a daemon running
on src.couchbase.org where I could push new revisions to, and
it would automatically add them to the indexed base.

As with a lot of other projects I never got around to
implement it all the way due to being too busy on other
projects (and I haven't use Java for a lot of years making
me spend more time on Google than actually getting the crap
done).

After writing a skeleton in Java I figured that it would be
fun to play around with Kotlin instead (yes, I have always
wanted to build an Android app, but never found time to that
either). Luckily IntelliJ could "convert" my java files to
Kotlin, and it wasn't that hard fixing the remaining bits
to make it compile and work.

What I ended up building was a program I drive from cron
which updates the source repositories and finally run
the OpenGrok indexer.

Couchbase server utilize `Google repo` to stitch together
a bunch of different git repositories into a final product.
As most other projects the server consists of both open
and closed source projects, and we only want to index the
open source projects on src.couchbase.org.

To do that we start off by cloning https://github.com/couchbase/manifest
(or `git remote update && git reset --hard origin/master` if the
repository is already there) and then rewrite all the manifest
files by adding a `thirdparty` group to all the external
dependencies we don't want to fetch and commit those. 

Given that we want to index a bunch of Couchbase servers which
is based on the same git repository we create a repo cache
we update.

Once that is in place we can search the manifest repository
and pick out all the couchbase server versions we want
to index and run repo init for new repositories and repo
sync for all the repositories which is still in development
before kicking off the indexer

