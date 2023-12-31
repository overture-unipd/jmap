default:
	@just --list

build:
	gradle dockerBuildImage
	docker build -t overture-unipd/caddy:latest -f caddy.dockerfile .

up *CONTAINERS:
	docker compose up -d {{CONTAINERS}}

down:
	docker compose down

destroy:
	docker compose down --remove-orphans --volumes

wire:
	sudo wireshark -f 'host localhost and port 8000'

run:
	bash -c "source .env && DB_HOST=localhost && export \$(cut -d= -f1 .env) && gradle run"

reset:
	bash -c 'source .env && curl https://${DOMAIN}/reset'

shell:
	gradle --console plain jshell
