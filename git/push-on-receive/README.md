# GIT - Push-on-receive
This base script is meant to be installed in a git repository. The purpose of the script
is to observe whether API Fortress tests are pushed to the repository and if they are,
it sends them to an API Fortress instance to be made "official", scheduled and executed
according to the users' needs.


## Installation
* Copy the `post-receive.py` file in the `hooks/` directory, removing the `.py` extension
* Copy the `configuration.py` file

## Configuration
* `test_subdirectory`: where the API Fortress tests are stored in the repository
* `default_hook`: a catch-all API hook to push the tests to. Change URL and headers according to your settings.
* `hooks_by_branch`: a collection of branches to push the tests to, based on the branch. Change URLs and headers according to your settings.
