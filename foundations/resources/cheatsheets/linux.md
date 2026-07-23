# Linux Cheatsheet

## Navigate & inspect

```bash
ls -lah                    # list all, human sizes
du -sh * | sort -h         # what's big in this dir
df -h                      # disk usage by mount
find . -name "*.log" -mtime -1     # logs modified < 24h
stat file                  # timestamps, perms, inode
```

## Text power tools

```bash
grep -rn "TODO" src/               # recursive search with line numbers
grep -E "ERROR|FATAL" app.log      # regex
tail -f app.log                    # follow a log live
less +F app.log                    # follow, Ctrl-C to scroll back
awk '{print $1, $9}' access.log    # pick columns
sed -i 's/old/new/g' file          # in-place replace
sort | uniq -c | sort -rn          # frequency count (top talkers)
cut -d: -f1 /etc/passwd            # split by delimiter
jq '.items[].name' data.json       # JSON surgery
```

## Processes & resources

```bash
ps aux --sort=-%mem | head         # memory hogs
top / htop                         # live view
kill -TERM <pid>                   # polite stop (then -KILL if ignored)
nice -n 10 cmd                     # run low priority
free -h                            # RAM
uptime                             # load average (vs nb of cores)
```

## Network

```bash
ss -tulpn                          # what's listening, which process
curl -v https://api.example.com    # request with full handshake detail
curl -s -o /dev/null -w "%{http_code} %{time_total}s\n" URL   # status+timing
ip a                               # interfaces & IPs
ping / traceroute host
dig example.com +short             # DNS lookup
```

## Permissions

```bash
chmod u+x script.sh        # user can execute
chmod 640 file             # rw- r-- ---
chown app:app /srv/data
sudo -u app whoami         # run as another user
umask                      # default perms for new files
```

## Services & logs (systemd)

```bash
systemctl status nginx
systemctl restart nginx
systemctl enable --now myapp      # start now + at boot
journalctl -u myapp -f            # follow a service's logs
journalctl --since "1 hour ago" -p err
```

## Survival combos

```bash
# Top 10 IPs in an access log
awk '{print $1}' access.log | sort | uniq -c | sort -rn | head

# Which process eats the disk with open deleted files
lsof +L1

# Watch a command every 2s
watch -n2 'ss -s'

# Safe long task over ssh
nohup ./long_job.sh > job.log 2>&1 &
```
