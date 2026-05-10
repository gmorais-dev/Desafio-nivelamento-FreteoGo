export function cn(...classes) {
  return classes.filter(Boolean).join(" ");
}

export function currency(value) {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

export function kg(value) {
  return new Intl.NumberFormat("pt-BR").format(value) + " kg";
}
