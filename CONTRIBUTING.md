Thank you for considering contributions towards Chouten, we appreciate all the help we can get!
That said, though the project was pretty much our first tread into the Android world and helped us get
to grips with development, it may be too advanced for a beginner to work on.
If you still feel up to the task, please, continue reading.

Want to actually speak with someone? Come speak with us on our [Discord Server](https://discord.gg/invite/eQFZasBs)
<br>
<br>
[<img src="https://invidget.switchblade.xyz/FAkzq5p35C" float="left" />](https://discord.gg/FAkzq5p35C)

**Table Of Contents:**
1. [Working with the App](#working-with-the-app)
	1. [Building the App](#building-the-app)
	2. [Structure](#structure)
	3. [Debugging](#debugging)
	4. [Working with the Crash Handler](#working-with-the-crash-handler)
2. [Pull Requests](#pull-requests)
	1. [Prerequisites](#prerequisites)
	2. [Build](#build)
	3. [Formatting](#formatting)
	4. [Review Process](#review-process)

# Working with the App
This section will describe how to work with the app itself - rather than how to contribute.
If you're unfamiliar with Android development, it'd be a good use of your time to read this.

**Contents**:
1. [Building the App](#building-the-app)
2. [Structure](#structure)
3. [Debugging](#debugging)
4. [Working with the Crash Handler](#working-with-the-crash-handler)

### Building the App
Before you can build the app, you'll need to create a `local.properties` file in the root project folder
and assign a value to `bug_webhook`. This url is used for reporting crash reports and is private. A properties file
with the following should suffice.
```
bug_webhook=https://example.com
```
Once you have done that, you should be able build the app perfectly fine.

### Structure
The app attempts to stick closely to the CLEAN architecture. Many articles can be found online
which explain the purpose of this and how to use it; [Phillip Lackner](https://www.youtube.com/@PhilippLackner) has some videos using it - namely, these two below:
-	[Making a Note App using Jetpack Compose & CLEAN Architecture](https://www.youtube.com/watch?v=8YPXv7xKh2w)
- [Making a Stock Market App using Jetpack Compose](https://www.youtube.com/watch?v=uLs2FxFSWU4)

Not only does it make the app more readable, it, in my opinion, makes it easier to write **better** code.
Knowing that you're writing code which should only handle business logic gives you a clear separation of concerns.
This separation is so strong that we can easily tear one implementation of a repository out and replace it with another.
This is very useful when testing, as we can override methods to provide mock data.
Furthermore, we use [dependency injection](https://www.freecodecamp.org/news/a-quick-intro-to-dependency-injection-what-it-is-and-when-to-use-it-7578c84fa88f) to provide the correct implementation of things throughout the app.

The implementation handles the business logic, and the use cases then do application specific things.
For example, with module installation, the implementation simply grabs all directories within the modules directory.
It doesn't (and shouldn't) check things such as the format version - nor should it parse the module itself.
It's under the belief that the modules directory only contains modules as it should. The use case then
handles the checking of each module and parsing it. The view models then utilise the use cases, rather than directly
interfacing with the repository. This means that any stage, we can switch the code out. Switching out the repository implementation is trivial, along with the use cases. 

Along with the Clean architecture, the app uses comments where needed (please let us know if you think otherwise!) and
uses self documenting names to make the lives of contributors easier.
Most/all methods should include the possible exceptions thrown. This should include exceptions that the 
function itself throws in code, but also exceptions that can be thrown by functions it calls upon.
This makes it easier for the developer to know which exceptions they should handle and will reduce
crashes for the end user.

### Debugging
As mentioned above, following the Clean architecture guidelines makes it easy for us to test.
This is one method of debugging & testing within the app; however, faking mock data is not always useful for
testing if your real data works.

#### Debugging the Webview

The webview is always debuggable at the moment; however, you can check in your own version
by looking for the following somewhere within the webview implementation.
```kt
WebView.setWebContentsDebuggingEnabled(true)
```
If this is set, you can use chrome's built-in `chrome://inspect` to see the injected content.
It may also prove useful to alter the common code javascript (within `res/raw`) to log more than usual.
Console logs within the module code are reflected into the logcat, so make sure to utilise that when possible.


#### Chouten CLI
If you don't want to keep rebuilding the app, you can use the [chouten package from npm](https://www.npmjs.com/package/chouten) for easy testing of a module.
If you want to test the module on your device/emulator, push the module into the correct directory.
```sh
# ONLY RUN THIS IF THE MODULE IS ZIPPED IN THE .module FORMAT
[chouten-contibutor:~chouten]$ unzip <module-name>.module -d <module-name>
# IF IT'S ALREADY UNZIPPED, START HERE
[chouten-contibutor:~chouten]$ adb push <module-name> <directory for modules>
# e.g, if using Documents/Chouten as the data directory, run:
# adb push <module-name> /storage/emulated/0/Documents/Chouten/Modules
```

#### Working with the Crash Handler
We use [ACRA](https://github.com/ACRA/acra) along with a custom reporter to
report our crash reports on discord (with a webhook). Since this webhook is private, to abuse spam,
you will not have it within your project.
If you would like to work on the crash reporter and test it, you can either add your own webhook to `local.properties`,
or ask one of us on our Discord and we'll create a private channel for you to test with.
The data structure for the webhook should contain most fields you will want to use - if you need more,
you can find them and their documentation over on the [discord developers site](https://discord.com/developers/docs/resources/webhook#execute-webhook).

---

# Pull Requests
We welcome pull requests, but have a few of our own **requests** for you!
The following will show a selection of steps you should follow during your PR.

**Contents:**
1. [Prerequisites](#prerequisites)
2. [Build](#build)
3. [Formatting](#formatting)
4. [Review Process](#review-process)

### Prerequisites

**Before you even think of attempting a PR, make sure that someone
else is not working on the same thing!** Check through the [PR list](https://github.com/Chouten-App/Chouten-Android-Clean/pulls) or on the issue that you're working on. If you're still not sure, come ask in our [Discord](https://discord.gg/invite/eQFZasBs)!
If you're working on an issue and no-one else appears to have started working on it, please
comment to make others aware that you're picking it up.

Now that you're sure you can work on your PR, there's a few things you'll want (and need)!
A basic understanding of:
-	[Android Development](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose/tutorial)
-	[Kotlin](https://kotlinlang.org/)
- [Git](https://git-scm.com/)[(Hub)](https://docs.github.com/en/get-started/quickstart/hello-world)

Jetpack Compose is the framework we use for our UI. For those coming from XML, you'll
find it quite different. Compose is declarative and more akin to Flutter than XML.
We use Git & GitHub for version control management, so make sure you're familiar
with at least the basics.

We also recommend that you use the following:
- [Android Studio (Android Development IDE)](https://developer.android.com/studio)
- An Emulator / Physical Device (preferred)

A physical device is recommended over an emulator simply due to an emulator hogging
so many resources on your system. 16/32gb of RAM is ideal for using an emulator.
The main pro of using an emulator is that you're able to change the version of android
you're using at will - making it great for testing if changes are backwards compatible or not!

If your PR involves fixing a bug, and you're not sure where it started, there's a couple of git commands that may help you:
```sh
# To view the changes to a specific file over time, you can use the follow argument
[chouten-contibutor:~chouten]$ git log -p --follow -- <file>
```
Learn more about `git log` [here](https://git-scm.com/docs/git-log)!
```sh
# To identify which commit(s) introduce a bug, you can use git bisect
# The command is fairly helpful, so you should be able to figure
# out how to use it beyond this from the help text alone.
[chouten-contibutor:~chouten]$ git bisect start
```
Learn more about `git bisect` [here](https://git-scm.com/docs/git-bisect)!

---

### Build

When needed, increase the version number in [`app/build.gradle.kts`](./app/build.gradle.kts) and, in accordance with
the [semantic versioning specification](https://semver.org), the version code. To prevent you from pushing without increasing the
version, you can use a hook, such as the following, within your `pre-push`.
If you don't think you need to update the version code (e.g you have only changed
documentation), you can run `git push --no-verify`
<details>
	<summary>Force Version Update Hook</summary>

```sh
#!/bin/sh
 An example hook script to verify what is about to be pushed.  Called by "git
# push" after it has checked the remote status, but before anything has been
# pushed.  If this script exits with a non-zero status nothing will be pushed.
#
# This hook is called with the following parameters:
#
# $1 -- Name of the remote to which the push is being done
# $2 -- URL to which the push is being done
#
# If pushing without using a named remote those arguments will be equal.
#
# Information about the commits which are being pushed is supplied as lines to
# the standard input in the form:
#
#   <local ref> <local oid> <remote ref> <remote oid>
#

remote="$1"
url="$2"

local_vers=$(cat app/build.gradle.kts | sed -nE 's/versionCode\s*=\s*(\w)/\1/p' | tr -d ' ')
remote_vers=$(git show $remote/dev:app/build.gradle.kts | sed -nE 's/versionCode\s*=\s*(\w)/\1/p' | tr -d ' ')
if [ $local_vers -le $remote_vers ]; then
	printf "\033[31;1m%s\033[0;0m\n" "Version Code (app/build.gradle.kts) has not been updated!"
	exit 1
fi
```

</details>
If you have no need to use the repo's workflows (e.g build), please don't waste
our precious minutes.

```sh
[chouten-contibutor:~chouten]$ git commit -m "docs: add CONTRIBUTING.md [skip ci]"
```
Adding it in the title of the commit, as shown above, will prevent the CI from running.
You can also include it within the description of the commit - either will do.

---

### Formatting
1. [Formatting your Code](#formatting-your-code)
2. [Formatting your Commits](#formatting-your-commits)
	1. [Fancy Commit Message Hook](#fancy-commit-messages-hook)

#### TL;DR:
- Don't introduce extra work for the reviewer (e.g mass formatting of existing code)
- Commit frequently and adhere to the guidelines in [conventional commits (1.0) specification](https://www.conventionalcommits.org/en/v1.0.0/#summary)
- Don't waste precious CI minutes - use `[skip ci]` in the commit message when needed.

#### Formatting your Code.
**Firstly**, to make things *easier* for the reviewer(s) to understand and read, we ask that
you ensure you've not created a huge diff!
For example, try not to cause something like below.
```diff
	val x = 10;
-	if (x == 10) {
+	if (x == 10)
+	{
-	  println("x is 10!");
+	  println("x is 10!")
	}
```
It's not too big of a deal here; however, the larger the commit and the more files
that there are to review, this can become annoying. This is often caused by having a different
formatting configuration to the one used within the repository.
If you are using Android Studio (highly recommended), you should be able to configure this
to inherit the formatting of the project. **Just make sure that when you commit, you're also removing 
any forced line breaks (pressing CTRL+ALT+L twice in Android Studio should do the trick).**
Note: if you don't need to, please don't add any hunks which change formatting unrelated to your
pull; a separate PR for a **chore**/**style** commit.

#### Formatting your Commits
Great, you've formatted your code nicely - maybe a few missing comments here and there and some
unusual styling, but, you've done it. **But have you formatted your commit messages?**
To check,	make sure your commit messages follow the style defined in the [conventional commits (1.0) specification](https://www.conventionalcommits.org/en/v1.0.0/#summary). We don't have any restrictions on tense, but as a bare minimum we'd like you to use the appropriate commit type (e.g feat).
If you don't trust yourself, you can try using the one of the git hooks below.

<details>
	<summary><h3>Commit Messages Hook</h3></summary>

Make sure you set this as the `commit-msg (.git/hooks/commit-msg)` hook!
```python
#!/usr/bin/env python3
import sys

def emoji_line(line_pieces: list, emoji: str) -> str:
    # If there is already an emoji in the same position, don't add another one
    # The emoji would be in the second part of the split
    if len(line_pieces) > 1 and line_pieces[1].strip().startswith(type_map.get(type)):
        return f'{line_pieces[0]}: {" ".join(line_pieces[1:]).strip()}'
    return f'{line_pieces[0]}: {emoji} {" ".join(line_pieces[1:]).strip()}'

commit_message_path = sys.argv[1]
new_commit_message: str = ""

# Note: This is just a hacky version of the emoji hook, with the emojis being blank
type_map = {
    "feat": "",
    "fix": "",
    "docs": "",
    "merge": "",
    "style": "",
    "refactor": "",
    "perf": "",
    "test": "",
    "chore": "",
    "revert": "",
    "build": "",
    "ci": "",
    "wip": "",
    "release": "",
    "workflow": "",
    "deps": "",
}

def get_valid_types() -> str:
    keys = list(type_map.keys())
    # Example
    #\t- feat
    #\t- fix
    # ...
    return "\t- " + "\n\t- ".join(keys)

with open(commit_message_path, "r") as commit_message_file:
    lines = commit_message_file.readlines()

    ########################################
    #       Check if commit is empty       #
    ########################################
    if len(lines) == 0:
        print("Commit message is empty")
        sys.exit(1)
    
    line_pieces = lines[0].split(":")

    ########################################
    #       Check if commit has a type     #
    ########################################
    if len(line_pieces) == 1 or line_pieces[0].strip() == "":
        print("\x1b[31;1mCommit message does not have a type\x1b[0;0m")
        print("USAGE: \x1b[;1m<type>(<scope>): <subject>\x1b[0m")
        print("Examples:\n\t\x1b[;1mfeat\x1b[0m: add new feature")
        print("\t\x1b[;1mfeat(test)\x1b[0m: add new testing features")
        # Use type_map.keys() to get all available types
        print("\n\x1b[33;1mAvailable types:\x1b[0;0m")
        print(get_valid_types())

        sys.exit(1)
    ########################################
    #   Check if commit subject is valid   #
    ########################################
    elif line_pieces[1].strip() == "":
        print("\x1b[31;1mCommit message does not have a subject\x1b[0;0m")
        print("USAGE: \x1b[;1m<type>(<scope>): <subject>\x1b[0m")
        print("Examples:\n\tfeat: \x1b[;1madd new feature\x1b[0m")
        print("\tfeat(test): \x1b[;1madd new testing features\x1b[0m")
        sys.exit(1)

    ########################################
    #   Check if commit type is valid      #
    ########################################
    type = line_pieces[0].lower().strip()
    scope = type.split("(")[1].split(")")[0] if "(" in type and ")" in type else None
    type = type.split("(")[0] if scope else type

    if type not in type_map.keys():
        print(f"\x1b[31;1mInvalid commit type: \x1b[31;3m{type}\x1b[0;0m")
        print(get_valid_types())
        sys.exit(1)

    ########################################
    #     Add emoji to commit message      #
    ########################################
    new_commit_messages = emoji_line(line_pieces, type_map[type]) + "\n" + "\n".join([str(l.strip()) for l in lines[1::]])

with open(commit_message_path, "w") as commit_message_file:
    commit_message_file.write(new_commit_messages)

sys.exit(0)
```
</details>
<details>
	<summary><h3>Commit Messages Hook (+ Emojis)</h3></summary>

Make sure you set this as the `commit-msg (.git/hooks/commit-msg)` hook!
```python
#!/usr/bin/env python3
import sys

def emoji_line(line_pieces: list, emoji: str) -> str:
    # If there is already an emoji in the same position, don't add another one
    # The emoji would be in the second part of the split
    if len(line_pieces) > 1 and line_pieces[1].strip().startswith(type_map.get(type)):
        return f'{line_pieces[0]}: {" ".join(line_pieces[1:]).strip()}'
    return f'{line_pieces[0]}: {emoji} {" ".join(line_pieces[1:]).strip()}'

commit_message_path = sys.argv[1]
new_commit_message: str = ""

type_map = {
    "feat": "🎉",
    "fix": "🐛",
    "docs": "📝",
    "merge": "🔗",
    "style": "🎨",
    "refactor": "♻️ ",
    "perf": "⚡️",
    "test": "🚨",
    "chore": "🔧",
    "revert": "⏪",
    "build": "👷",
    "ci": "👷",
    "wip": "🚧",
    "release": "🏁",
    "workflow": "📦",
    "deps": "⬆️",
}

def get_valid_types() -> str:
    keys = list(type_map.keys())
    # Example
    #\t- feat
    #\t- fix
    # ...
    return "\t- " + "\n\t- ".join(keys)

with open(commit_message_path, "r") as commit_message_file:
    lines = commit_message_file.readlines()

    ########################################
    #       Check if commit is empty       #
    ########################################
    if len(lines) == 0:
        print("Commit message is empty")
        sys.exit(1)
    
    line_pieces = lines[0].split(":")

    ########################################
    #       Check if commit has a type     #
    ########################################
    if len(line_pieces) == 1 or line_pieces[0].strip() == "":
        print("\x1b[31;1mCommit message does not have a type\x1b[0;0m")
        print("USAGE: \x1b[;1m<type>(<scope>): <subject>\x1b[0m")
        print("Examples:\n\t\x1b[;1mfeat\x1b[0m: add new feature")
        print("\t\x1b[;1mfeat(test)\x1b[0m: add new testing features")
        # Use type_map.keys() to get all available types
        print("\n\x1b[33;1mAvailable types:\x1b[0;0m")
        print(get_valid_types())

        sys.exit(1)
    ########################################
    #   Check if commit subject is valid   #
    ########################################
    elif line_pieces[1].strip() == "":
        print("\x1b[31;1mCommit message does not have a subject\x1b[0;0m")
        print("USAGE: \x1b[;1m<type>(<scope>): <subject>\x1b[0m")
        print("Examples:\n\tfeat: \x1b[;1madd new feature\x1b[0m")
        print("\tfeat(test): \x1b[;1madd new testing features\x1b[0m")
        sys.exit(1)

    ########################################
    #   Check if commit type is valid      #
    ########################################
    type = line_pieces[0].lower().strip()
    scope = type.split("(")[1].split(")")[0] if "(" in type and ")" in type else None
    type = type.split("(")[0] if scope else type

    if type not in type_map.keys():
        print(f"\x1b[31;1mInvalid commit type: \x1b[31;3m{type}\x1b[0;0m")
        print(get_valid_types())
        sys.exit(1)

    ########################################
    #     Add emoji to commit message      #
    ########################################
    new_commit_messages = emoji_line(line_pieces, type_map[type]) + "\n" + "\n".join([str(l.strip()) for l in lines[1::]])

with open(commit_message_path, "w") as commit_message_file:
    commit_message_file.write(new_commit_messages)

sys.exit(0)
```
</details>

Doing this means that it's much easier for others to find
your commit by the type it was. If everyone follows the same format, it makes
it much easier to filter through commits!<br>
**Remember to also break your commits down. Don't go overboard - that goes for too many and too little.**

----

### Review Process
**NOTE: Not all PRs will be accepted. Please check beforehand by either contacting
us through a platform such as Discord or opening an issue on this repo!**

1. [Personal Review](#personal-review)
2. [Our Review](#our-review)

#### Personal Review

If you're still adding things to the PR and don't require a review yet, please utilise
the [Draft PR](https://github.blog/2019-02-14-introducing-draft-pull-requests/) feature.
If you have a lot of commits, make sure you clean up the titles and commits themselves.
For example, if your PR contains a commit history something like the example below, you will not get
merged and may not get a basic review (until amended).
```commit
* 7df7992 Update build.gradle.kts
* 3ae2df1 Update build.gradle.kts
* 2dbcfcd correct spelling in SearchView.kt
* 8627f95 correct spelling in InfoView.kt
* 1982fa1 add feature to WatchView. use cached file instead of refetching another....
```
Performing a rebase would allow you to squash, rename and remove commits.
Before rebasing, it may be a good idea to create a copy of your branch - just in case
you severely mess anything up; however, the git reflog command can also help you revert to a previous
state.
If you have forked from the main branch, you can run the following to start an interactive rebase on all
of your commits.
`git rebase -i $(git rev-parse dev)`
An amended history for the previous example would look something like the following:
```commit
* 7a17a92 chore(version): update version code
* 1a9f182 chore: correct spellings
* 91a0b11 feat(WatchView): use cached media data
```
We've condensed the two spelling fixes into a single commit (which can still contain the previous commit messages),
and followed the conventional commit guidelines. The last commit `use cached media data` has been reduced
to a much more concise message; however, this does not mean you should leave out important details just to 
reduce the commit title! You can use the commit body to explain the change in more detail if required!
```commit
feat(WatchView): use cached media data

Cache and store the media data if none exists. Prevents
using the network when it is not required. Cache expires
after 30 mins to prevent stale data but should still reduce
the load on the user's network - especially useful for those on mobile data.

# Please enter the commit message for your changes. Lines starting
# with '#' will be ignored, and an empty message aborts the commit.
#
# Date:      Wed Nov 15 15:55:48 2023 +0000
#
# On branch feat-use-cached-data
# Changes to be committed:
#	modified:   app/src/main/java/com/chouten/app/presentation/ui/screens/watch/WatchView.kt
#
```
After rebasing, a force push would likely be required (as the commit history would be different). Force pushing to your PR is fine, as we will be able to see if it causes issues with the dev branch
before attempting to merge.

#### Our Review

**All** commits are done using pull requests. This allows us to make sure
that each commit is properly reviewed by (a minimum of) **2 reviewers**.
During this process, they will review the authenticity of the code to
ensure that there is no malicious additions.
The quality of the code will be assessed and, assuming everything went well,
your PR will be merged into the development branch (dev).

