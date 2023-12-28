default:
	@just --list

build:
	gradle dockerBuildImage

up *CONTAINERS:
	docker compose up -d {{CONTAINERS}}

down:
	docker compose down

destroy:
	docker compose down --remove-orphans --volumes

wire:
	sudo wireshark -f 'host localhost and port 8000'

run:
	bash -c "source env/overture && DATABASE=localhost && export \$(cut -d= -f1 env/overture) && gradle run"

reset:
	curl localhost:8000/reset

shell:
	gradle --console plain jshell