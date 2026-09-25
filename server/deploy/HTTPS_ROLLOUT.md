# HTTPS on the existing friend-hosted Aliyun server

Target: `8.133.216.39`; current API: `http://8.133.216.39:8080`.
This plan is prepared, not deployed. On September 25, 2026 ports 22, 80 and
8080 accepted TCP connections; port 443 refused the connection. Server login
is required before inspecting or changing the existing services.

An IP certificate is now possible without buying a domain. Let's Encrypt
[supports IP certificates](https://letsencrypt.org/2026/01/15/6day-and-ip-general-availability);
they last 160 hours, so automatic renewal and reload are mandatory. Use
[Certbot 5.4 or later for webroot IP validation](https://letsencrypt.org/2026/03/11/shorter-certs-certbot).
Certificate issuance does not establish app-store, filing or other regulatory eligibility.

## Inspect before changing

Confirm the OS, Java service/container, active JAR revision, data directory,
existing Nginx/other listeners, firewall/security group, certificate tooling,
logs, backup jobs and cloud snapshots. Do not print environment secrets or
player snapshots. Do not replace another site's port-80 configuration or stop
the existing game process to obtain a certificate. Confirm the original owner
permits using this public IP and adding an HTTPS virtual host.

## Add HTTPS without moving player data

1. Add only the port-80 server block from `nginx-ip.conf.example`, adapted to
   the existing Nginx installation and checked for conflicting server names.
   Create `/var/lib/yigame-acme/.well-known/acme-challenge/`, place a disposable
   probe there and verify external HTTP access before certificate issuance.
2. After the operator has accepted the CA subscriber terms, use the installed
   Certbot to request an IP certificate. Test against staging first:

   ```sh
   certbot certonly --staging --preferred-profile shortlived \
     --webroot --webroot-path /var/lib/yigame-acme \
     --ip-address 8.133.216.39 --cert-name yigame-ip-staging
   ```

   Request the trusted certificate with the same validation options, omitting
   `--staging` and using `--cert-name yigame-ip`. Do not install a staging
   certificate in the production virtual host.
3. Add the port-443 block, run `nginx -t`, then reload Nginx. Open 443 in the
   applicable firewall/security group. Keep the original Java data directory,
   room credentials and existing 8080 service during compatibility verification.
4. Configure the installed Certbot renewal timer and a deploy hook which runs
   `nginx -t && systemctl reload nginx`. Inspect its actual schedule, test a
   dry-run renewal with deploy hooks, and verify the served certificate after
   reload. Use the existing monitoring system to alert on failed renewal and
   low remaining certificate lifetime. Do not rely on a one-off certificate.
5. Verify trusted certificate chain and IP SAN from outside the server. Check
   `/health`, all six games, private role isolation, host mode notifications,
   reconnects, invitations, and HTTPS Web same-origin API requests. Confirm
   public `/api/v1/admin` routes are blocked.
6. Only after HTTPS passes, update Android/KMP and Harmony defaults to
   `https://8.133.216.39`, handle saved sessions pointing at the old default
   without changing custom servers, and test the actual release packages.
   Set `PUBLIC_WEB_URL` to the validated HTTPS Web origin when Web is deployed.
7. Retire external 8080 only after the old-client compatibility decision; do
   not silently break existing sessions. Bind Java to loopback when retired.

## Retention must match reality

The supplied HTTPS example disables access logs for this virtual host. Error
logs can still contain IPs and paths; set a dedicated log rotation policy after
reviewing actual operational/legal requirements. Verify journal/container logs,
host-level log forwarding and provider snapshots separately. Do not delete
backups or truncate player data as an automatic side effect of this deployment.

Record the applied log/backup retention and the actual deletion mechanism, then
update `docs/store/privacy-full.txt`, regenerate all policy copies and publish.
Until those checks and HTTPS tests pass, neither the policy nor the Play Data
Safety form may claim that the production setup has been verified or that all
room traffic is encrypted.
