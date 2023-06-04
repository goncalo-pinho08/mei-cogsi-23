#! /usr/bin/python
""" testing vault.py, the script that mimics a minimized
    version of HashiCorp Vault's client tool 'vault' """

import os
import sys
import re
import time
import inspect
import unittest
import subprocess
import shutil
import collections
import logging
from fnmatch import fnmatchcase as fnmatch

logg = logging.getLogger("tests")


loginfile = "~/.vault_token"
_vault_py = os.path.dirname(__file__)+"/vault.py"
_python = "python"

def vault():
    return "%s %s " % (_python, _vault_py)

def get_caller_name():
    frame = inspect.currentframe().f_back.f_back
    return frame.f_code.co_name
def get_caller_caller_name():
    frame = inspect.currentframe().f_back.f_back.f_back
    return frame.f_code.co_name
def sh(cmd, env = {}, shell = True):
    Shell = collections.namedtuple("Shell", ["returncode", "stdout", "stderr" ])
    envs = os.environ.copy()
    envs.update(env)
    logg.debug(" ... %s", cmd)
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=shell, env = envs)
    proc.wait()
    return Shell(proc.returncode, proc.stdout.read(), proc.stderr.read())
def proc(cmd, env = {}, shell = True):
    Shell = collections.namedtuple("Shell", ["returncode", "stdout", "stderr" ])
    envs = os.environ.copy()
    envs.update(env)
    logg.debug(" ... %s", cmd)
    return subprocess.Popen(cmd, shell=shell, env = envs)

class VaultTests(unittest.TestCase):
    def caller_testname(self):
        name = get_caller_caller_name()
        x1 = name.find("_")
        if x1 < 0: return name
        x2 = name.find("_", x1+1)
        if x2 < 0: return name
        return name[:x2]
    def testname(self, suffix = None):
        name = self.caller_testname()
        if suffix:
            return name + "_" + suffix
        return name
    def testport(self, testname = None):
        testname = testname or self.caller_testname()
        m = re.match("\w*_(\d+).*", testname)
        if m:
            return int(m.group(1)) + 6000
        return 6400 + (time.time() % 100)
    def testdir(self, testname = None, keep = False):
        testname = testname or self.caller_testname()
        newdir = "tmp/tmp."+testname
        if os.path.isdir(newdir) and not keep:
            shutil.rmtree(newdir)
        if not os.path.isdir(newdir):
            os.makedirs(newdir)
        return newdir
    def rm_testdir(self, testname = None):
        testname = testname or self.caller_testname()
        newdir = "tmp/tmp."+testname
        if os.path.isdir(newdir):
            shutil.rmtree(newdir)
        return newdir
    def envs(self, tmp):
        return { "VAULT_LOGINFILE": tmp + "/vault_token",
                 "VAULT_DATAFILE": tmp + "/vault_data.ini" }
    def show(self, done):
        logg.debug("\n ==STDOUT==\n%s\n ==STDERR==\n%s\n ==DONE %s==", done.stdout.strip(), done.stderr.strip(), done.returncode)
    #
    def test_001_config(self):
        """ allow for 'config' """
        tmp = self.testdir()
        env = self.envs(tmp)
        cmd = vault() + "config"
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        # self.assertIn(env["VAULT_LOGINFILE"], done.stdout)
        self.assertIn(env["VAULT_DATAFILE"], done.stdout)
        self.rm_testdir()
    def test_002_config_table(self):
        """ allow for 'config' """
        tmp = self.testdir()
        env = self.envs(tmp)
        cmd = vault() + "config -format=table"
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertIn(env["VAULT_LOGINFILE"], done.stdout)
        self.assertIn(env["VAULT_DATAFILE"], done.stdout)
        self.rm_testdir()
    def test_101_login(self):
        """ any 'login' possible """
        cmd = vault() + "login foo -v -v"
        tmp = self.testdir()
        env = self.envs(tmp)
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_LOGINFILE"]))
        value = open(env["VAULT_LOGINFILE"]).read()
        self.assertEqual(value.strip(), "foo")
        self.rm_testdir()
    def test_102_write(self):
        """ do 'write' any value """
        cmd = vault() + "write secret/test/foo value=bar"
        tmp = self.testdir()
        env = self.envs(tmp)
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        self.rm_testdir()
    def test_103_read(self):
        """ do 'read' that value """
        cmd = vault() + "read secret/test/foo -field=value"
        pre = vault() + "write secret/test/foo value=bar"
        tmp = self.testdir()
        env = self.envs(tmp)
        done = sh(pre, env)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertEqual(done.stdout, "bar")
        self.rm_testdir()
    def test_104_read_json(self):
        """ do 'read' that value as json """
        cmd = vault() + "read secret/test/foo -format=json"
        pre = vault() + "write secret/test/foo value=bar"
        tmp = self.testdir()
        env = self.envs(tmp)
        done = sh(pre, env)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertEqual(done.stdout.strip(), '{"data": {"value": "bar"}}')
        self.rm_testdir()
    def test_302_write_expired(self):
        """ do 'write' with expired """
        cmd = vault() + "write secret/test/bar value=foo expired=next"
        pre = vault() + "write secret/test/bar value=foo"
        tmp = self.testdir()
        env = self.envs(tmp)
        done = sh(pre, env)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.rm_testdir()
    def test_303_read(self):
        """ do 'read' that value """
        cmd = vault() + "read secret/test/bar -field=value"
        pre = vault() + "write secret/test/bar value=foo expired=next"
        tmp = self.testdir()
        env = self.envs(tmp)
        done = sh(pre, env)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertEqual(done.stdout.strip(), "foo")
        self.rm_testdir()
    def test_304_read_json(self):
        """ do 'read' that value as json and find 'expired' """
        cmd = vault() + "read secret/test/bar -format=json"
        pre = vault() + "write secret/test/bar value=foo expired=next"
        tmp = self.testdir()
        env = self.envs(tmp)
        done = sh(pre, env)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertEqual(done.stdout.strip(), '{"data": {"expired": "next", "value": "foo"}}')
        self.rm_testdir()
    def test_305_read_json(self):
        """ do 'read' that value as table """
        cmd = vault() + "read secret/test/bar -format=table"
        pre = vault() + "write secret/test/bar value=foo expired=next"
        tmp = self.testdir()
        env = self.envs(tmp)
        done = sh(pre, env)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertEqual(done.stdout, 'expired next\nvalue foo\n')
        self.rm_testdir()
    def test_443_read_oldstyle(self): # OBSOLETE
        """ do 'read' a value even without -field=value (some extra) """
        cmd = vault() + "read secret/test/foo"
        pre = vault() + "write secret/test/foo value=bar"
        tmp = self.testdir()
        env = self.envs(tmp)
        done = sh(pre, env)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        done = sh(cmd, env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertEqual(done.stdout, "bar\n")
        self.rm_testdir()
    def test_500_vault_server(self):
        """ do 'read' a value even without -field=value (some extra) """
        name = self.testname()
        port = self.testport()
        cmd = vault() + "read -address=http://127.0.0.1:{port} secret/{name}/foo -v -v -v -v"
        pre = vault() + "write secret/{name}/foo value=bar"
        run = vault() + "server -address=http://127.0.0.1:{port}"
        tmp = self.testdir()
        env = self.envs(tmp)
        done = sh(pre.format(**locals()), env)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        server = proc(run.format(**locals()), env)
        done = sh(cmd.format(**locals())) # no env! <<<<
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertEqual(done.stdout, "bar\n")
        self.rm_testdir()
        server.terminate()
    def test_501_vault_https_server_missing_key(self): 
        """ do 'read' a value even without -field=value (some extra) """
        name = self.testname()
        port = self.testport()
        cmd = vault() + "read -address=https://127.0.0.1:{port} secret/{name}/foo -v -v -v -v"
        pre = vault() + "write secret/{name}/foo value=bar"
        run = vault() + "server -address=https://127.0.0.1:{port}"
        tmp = self.testdir()
        env = self.envs(tmp)
        env2 = {}
        env2["VAULT_SKIP_VERIFY"] = "yes"
        done = sh(pre.format(**locals()), env)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        server = proc(run.format(**locals()), env)
        logg.info("server %s", server.returncode)
        time.sleep(1)
        logg.info("server %s", server.returncode)
        done2 = sh(cmd.format(**locals()), env2)
        self.show(done2)
        self.assertEqual(done2.returncode, 1)
        self.assertIn("Connection refused", done2.stderr)
        server.wait()
        logg.info("server done %s", server.returncode)
        self.assertEqual(server.returncode, 1)
        self.rm_testdir()
        # server.terminate()
    def test_502_vault_https_server(self): 
        """ do 'read' a value even without -field=value (some extra) """
        name = self.testname()
        port = self.testport()
        cmd = vault() + "read -address=https://127.0.0.1:{port} secret/{name}/foo -v -v -v -v"
        pre = vault() + "write secret/{name}/foo value=bar"
        run = vault() + "server -address=https://127.0.0.1:{port}"
        gen = " openssl req -new -x509 -keyout {tmp}/{name}.pem -out {tmp}/{name}.pem -days 365 -nodes -batch"
        tmp = self.testdir()
        env = self.envs(tmp)
        env["VAULT_SSL_KEY"] = "{tmp}/{name}.pem".format(**locals())
        env2 = {}
        env2["VAULT_SKIP_VERIFY"] = "yes"
        done = sh(gen.format(**locals()), env)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_SSL_KEY"]))
        done = sh(pre.format(**locals()), env)
        self.assertEqual(done.returncode, 0)
        self.assertTrue(os.path.exists(env["VAULT_DATAFILE"]))
        server = proc(run.format(**locals()), env)
        done = sh(cmd.format(**locals()), env2)
        self.show(done)
        self.assertEqual(done.returncode, 0)
        self.assertEqual(done.stdout, "bar\n")
        self.rm_testdir()
        server.terminate()

if __name__ == "__main__":
    from optparse import OptionParser
    _o = OptionParser("%prog [options] test*",
       epilog=__doc__.strip().split("\n")[0])
    _o.add_option("-v","--verbose", action="count", default=0,
       help="increase logging level [%default]")
    _o.add_option("--with", metavar="FILE", dest="vault_py", default=_vault_py,
       help="systemctl.py file to be tested (%default)")
    _o.add_option("-p","--python", metavar="EXE", default=_python,
       help="use another python execution engine [%default]")
    _o.add_option("-l","--logfile", metavar="FILE", default="",
       help="additionally save the output log to a file [%default]")
    _o.add_option("--xmlresults", metavar="FILE", default=None,
       help="capture results as a junit xml file [%default]")
    opt, args = _o.parse_args()
    logging.basicConfig(level = logging.WARNING - opt.verbose * 5)
    _vault_py = opt.vault_py
    _python = opt.python
    #
    logfile = None
    if opt.logfile:
        if os.path.exists(opt.logfile):
           os.remove(opt.logfile)
        logfile = logging.FileHandler(opt.logfile)
        logfile.setFormatter(logging.Formatter("%(levelname)s:%(relativeCreated)d:%(message)s"))
        logging.getLogger().addHandler(logfile)
        logg.info("log diverted to %s", opt.logfile)
    xmlresults = None
    if opt.xmlresults:
        if os.path.exists(opt.xmlresults):
           os.remove(opt.xmlresults)
        xmlresults = open(opt.xmlresults, "w")
        logg.info("xml results into %s", opt.xmlresults)
    # unittest.main()
    suite = unittest.TestSuite()
    if not args: args = [ "test_*" ]
    for arg in args:
        for classname in sorted(globals()):
            if not classname.endswith("Tests"):
                continue
            testclass = globals()[classname]
            for method in sorted(dir(testclass)):
                if "*" not in arg: arg += "*"
                if arg.startswith("_"): arg = arg[1:]
                if fnmatch(method, arg):
                    suite.addTest(testclass(method))
    # select runner
    if not logfile:
        if xmlresults:
            import xmlrunner
            Runner = xmlrunner.XMLTestRunner
            result = Runner(xmlresults).run(suite)
        else:
            Runner = unittest.TextTestRunner
            result = Runner(verbosity=opt.verbose).run(suite)
    else:
        Runner = unittest.TextTestRunner
        if xmlresults:
            import xmlrunner
            Runner = xmlrunner.XMLTestRunner
        result = Runner(logfile.stream, verbosity=opt.verbose).run(suite)
    if not result.wasSuccessful():
        sys.exit(1)
