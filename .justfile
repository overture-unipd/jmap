default:
	@just --list

build:
	gradle dockerBuildImage
	docker build -t overture-unipd/caddy:latest -f caddy.dockerfile .

up *CONTAINERS:
	docker compose up {{CONTAINERS}}

down:
	docker compose down

destroy:
	docker compose down --remove-orphans --volumes

wireshark:
	sudo wireshark -f 'host 127.0.0.1 and port 8000'

test:
	gradle test

run:
	bash -c "source .env && DB_HOST=localhost && export \$(cut -d= -f1 .env) && gradle run"

reset:
	bash -c 'source .env && curl https://${DOMAIN}/reset'

shell:
	rlwrap gradle --console plain jshell
