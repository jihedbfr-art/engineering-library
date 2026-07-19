# Ansible — Configuration Management

Terraform ([terraform-basics.md](terraform-basics.md)) answers "what infrastructure exists." Ansible answers a different question: "what state is running on it right now" — packages installed, files in place, services running, config applied. Where they overlap (both *can* provision a VM) isn't where either one is actually good — Terraform for the resource graph, Ansible for what happens once the box exists.

## Agentless, that's the whole pitch

No daemon running on the target — Ansible connects over SSH (or WinRM for Windows), pushes a Python payload, runs it, cleans up. Nothing to install ahead of time beyond Python itself, nothing to keep patched on a thousand hosts, no agent that can itself drift out of date and become the thing you have to manage.

## Playbooks — the core unit

```yaml
# site.yml
---
- name: Configure app servers
  hosts: app_servers
  become: true
  vars:
    app_user: deploy
  tasks:
    - name: Ensure app user exists
      ansible.builtin.user:
        name: "{{ app_user }}"
        shell: /bin/bash

    - name: Install required packages
      ansible.builtin.apt:
        name: ["openjdk-17-jre-headless", "curl"]
        state: present
        update_cache: true

    - name: Deploy application jar
      ansible.builtin.copy:
        src: files/app.jar
        dest: /opt/app/app.jar
        owner: "{{ app_user }}"
        mode: "0644"
      notify: restart app

  handlers:
    - name: restart app
      ansible.builtin.systemd:
        name: app
        state: restarted
```

```bash
ansible-playbook -i inventory.ini site.yml --check   # dry-run, ALWAYS do this first on prod
ansible-playbook -i inventory.ini site.yml
```

## Idempotency is the entire point

Every built-in module (`apt`, `copy`, `user`, `systemd`...) is written to be safe to run repeatedly: run the playbook once or a hundred times, the end state is the same, and a run where nothing needed to change reports zero changes rather than blindly re-executing every step. This is what makes a playbook something you can run on a schedule or after every deploy without worrying it'll break something that was already correct. The moment you reach for `ansible.builtin.shell` or `command` to do something the built-in modules don't cover, that guarantee is gone — those run every time regardless of state, and it's on you to add the `creates:`/`when:` guard that gets idempotency back.

```yaml
# Non-idempotent without a guard — runs every single time
- name: Initialize database
  ansible.builtin.shell: /opt/app/init-db.sh

# Fixed — only runs if the marker file doesn't already exist
- name: Initialize database
  ansible.builtin.shell: /opt/app/init-db.sh
  args:
    creates: /opt/app/.db-initialized
```

## Inventory — static or dynamic

Static (`inventory.ini` or `.yml`) is fine for a handful of long-lived servers. Past that, a **dynamic inventory** plugin (AWS EC2, Azure, GCP) queries the cloud provider's API at run time so the target list is always accurate — no manually-maintained file that's quietly out of date the day someone spins up a new instance and forgets to add it.

## Secrets — Ansible Vault

Never a plaintext password/API key in a playbook or inventory var file, even in a private repo — see [secrets-management.md](../security/secrets-management.md) for why that rule has no exceptions.

```bash
ansible-vault encrypt group_vars/prod/secrets.yml
ansible-playbook site.yml --ask-vault-pass
# or, in CI: --vault-password-file <(echo "$VAULT_PASSWORD")
```

## Where it sits next to Terraform in practice

Common split: Terraform provisions the VM/cluster and hands off its IP/hostname; Ansible takes that inventory and configures what runs on it. Terraform's `local-exec`/`remote-exec` provisioners can technically call Ansible directly, but coupling the two that tightly makes each harder to run or debug independently — cleaner to run them as two separate, sequential steps in the pipeline and let the dynamic inventory plugin pick up what Terraform just created.
