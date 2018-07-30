#!/usr/bin/env python

import fileinput
import subprocess
import json
import configuration
import ssl

try:
    from urllib.request import Request, urlopen  # Python 3
except ImportError:
    from urllib2 import Request, urlopen  # Python 2


def choose_hook(branch):
    if branch in configuration.hooks_by_branch:
        return configuration.hooks_by_branch[branch]
    return configuration.default_hook


def post_receive(from_commit, to_commit, branch_name):
    print("APIF: Checking for modified")

    p = subprocess.Popen("git rev-parse --symbolic --abbrev-ref "+branch_name, stdout=subprocess.PIPE, shell=True)
    branch_name = p.stdout.read().strip()
    p = subprocess.Popen('git show '+branch_name+' --pretty="" --name-status', stdout=subprocess.PIPE, shell=True)
    files = p.stdout.read()
    files = files.strip().split('\n')
    body = {'resources':[]}
    for f in files:
        items = f.split('\t')
        if (items[0] in ['A','M']) and items[1].startswith(configuration.test_subdirectory):
            fn = items[1]
            fn = fn[len(configuration.test_subdirectory):len(items[1])]
            p = subprocess.Popen('git show '+branch_name+':'+items[1],stdout=subprocess.PIPE,shell=True)
            content = p.stdout.read().strip()
            resource = {'path':fn,'branch':branch_name,'revision':to_commit,'content':content}
            body['resources'].append(resource)
    print("APIF: Updating "+str(len(body['resources']))+" remote test files")
    if len(body['resources']) > 0:
        chosen_hook = choose_hook(branch_name)
        params = json.dumps(body).encode('utf8')

        req = Request(chosen_hook['url']+'/tests/push', data=params)

        for h in chosen_hook['headers']:
            req.add_header(h['name'], h['value'])

        req.add_header('Content-Type','application/json')
        resp = urlopen(req)
        code = resp.getcode()
        if code==200:
            print("APIF: OK")
        else:
            print("APIF: "+code+" error")


fc,tc,bn = fileinput.input()[0].split()
post_receive(fc, tc, bn)
