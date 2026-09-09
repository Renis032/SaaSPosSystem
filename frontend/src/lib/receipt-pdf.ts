import { jsPDF } from 'jspdf'
import type { Receipt } from '@/types/models'
import type { ActiveBranch } from '@/lib/branch-store'

function money(value?: number) {
  return `$${(value ?? 0).toFixed(2)}`
}

function receiptTotal(receipt: Receipt) {
  if (receipt.totalAmount != null) return receipt.totalAmount
  return (receipt.items ?? []).reduce(
    (sum, item) => sum + (item.lineTotal ?? item.finalPrice ?? 0),
    0,
  )
}

export function downloadReceiptPdf(
  receipt: Receipt,
  options?: {
    branch?: ActiveBranch | null
    paymentType?: string
    tendered?: number
    change?: number
  },
) {
  const doc = new jsPDF({ unit: 'pt', format: 'letter' })
  const left = 48
  let y = 56

  doc.setFont('helvetica', 'bold')
  doc.setFontSize(18)
  doc.text(receipt.storeName || 'Renko POS', left, y)
  y += 22

  doc.setFont('helvetica', 'normal')
  doc.setFontSize(11)
  if (receipt.storeAddress) {
    doc.text(receipt.storeAddress, left, y)
    y += 16
  }
  if (receipt.storePhone) {
    doc.text(receipt.storePhone, left, y)
    y += 16
  }
  const branchName = receipt.branchName || options?.branch?.name
  if (branchName) {
    doc.text(`Branch: ${branchName}`, left, y)
    y += 16
  }

  y += 8
  doc.setFont('helvetica', 'bold')
  doc.text(`Receipt ${receipt.receiptNumber ?? `#${receipt.orderId}`}`, left, y)
  y += 18
  doc.setFont('helvetica', 'normal')
  doc.text(
    `Date: ${receipt.orderDate ? new Date(receipt.orderDate).toLocaleString() : '—'}`,
    left,
    y,
  )
  y += 16
  doc.text(`Cashier: ${receipt.cashierName ?? '—'}`, left, y)
  y += 16
  if (receipt.customerName) {
    doc.text(`Customer: ${receipt.customerName}`, left, y)
    y += 16
  }
  const payment = options?.paymentType || receipt.paymentType
  if (payment) {
    doc.text(`Payment: ${payment}`, left, y)
    y += 16
  }

  y += 10
  doc.setDrawColor(180)
  doc.line(left, y, 560, y)
  y += 22

  for (const item of receipt.items ?? []) {
    const label = `${item.quantity} × ${item.name}`
    const amount = money(item.lineTotal ?? item.finalPrice)
    doc.text(label, left, y)
    doc.text(amount, 560, y, { align: 'right' })
    y += 18
    if (y > 720) {
      doc.addPage()
      y = 56
    }
  }

  y += 8
  doc.line(left, y, 560, y)
  y += 22

  doc.setFont('helvetica', 'normal')
  if (receipt.subtotal != null) {
    doc.text('Subtotal', left, y)
    doc.text(money(receipt.subtotal), 560, y, { align: 'right' })
    y += 16
  }
  if (receipt.totalDiscount != null && receipt.totalDiscount > 0) {
    doc.text('Discount', left, y)
    doc.text(`-${money(receipt.totalDiscount)}`, 560, y, { align: 'right' })
    y += 16
  }
  if (receipt.taxRate != null && receipt.taxRate > 0) {
    doc.text(`Tax (${receipt.taxRate}%)`, left, y)
    doc.text(money(receipt.taxAmount), 560, y, { align: 'right' })
    y += 16
  }

  doc.setFont('helvetica', 'bold')
  doc.text('Total', left, y)
  doc.text(money(receiptTotal(receipt)), 560, y, { align: 'right' })
  y += 18

  if (options?.tendered != null) {
    doc.setFont('helvetica', 'normal')
    doc.text('Tendered', left, y)
    doc.text(money(options.tendered), 560, y, { align: 'right' })
    y += 16
    doc.text('Change', left, y)
    doc.text(money(options.change ?? 0), 560, y, { align: 'right' })
  }

  y += 28
  doc.setFontSize(10)
  doc.setTextColor(100)
  doc.text('Thank you for shopping with Renko.', left, y)

  const filename = `renko-receipt-${receipt.receiptNumber ?? receipt.orderId}.pdf`
  doc.save(filename)
}

export function printReceipt(
  receipt: Receipt,
  options?: {
    branch?: ActiveBranch | null
    paymentType?: string
    tendered?: number
    change?: number
  },
) {
  const payment = options?.paymentType || receipt.paymentType || ''
  const branchName = receipt.branchName || options?.branch?.name || ''
  const itemsHtml = (receipt.items ?? [])
    .map(
      (item) =>
        `<tr><td>${item.quantity} × ${escapeHtml(item.name)}</td><td class="amt">${money(item.lineTotal ?? item.finalPrice)}</td></tr>`,
    )
    .join('')

  const html = `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>Receipt ${escapeHtml(receipt.receiptNumber ?? String(receipt.orderId))}</title>
  <style>
    body { font-family: Georgia, "Times New Roman", serif; color: #111; margin: 24px; }
    h1 { font-size: 18px; margin: 0 0 6px; }
    p { margin: 0 0 4px; font-size: 12px; }
    table { width: 100%; border-collapse: collapse; margin: 16px 0; font-size: 13px; }
    td { padding: 4px 0; border-bottom: 1px dashed #ccc; }
    td.amt { text-align: right; white-space: nowrap; }
    .totals { margin-top: 8px; font-size: 13px; }
    .totals div { display: flex; justify-content: space-between; padding: 3px 0; }
    .totals .grand { font-weight: bold; font-size: 15px; margin-top: 6px; border-top: 1px solid #111; padding-top: 8px; }
    @media print { body { margin: 0; } }
  </style>
</head>
<body>
  <div class="receipt-print">
    <h1>${escapeHtml(receipt.storeName || 'Renko POS')}</h1>
    ${receipt.storeAddress ? `<p>${escapeHtml(receipt.storeAddress)}</p>` : ''}
    ${receipt.storePhone ? `<p>${escapeHtml(receipt.storePhone)}</p>` : ''}
    ${branchName ? `<p>Branch: ${escapeHtml(branchName)}</p>` : ''}
    <p><strong>Receipt ${escapeHtml(receipt.receiptNumber ?? `#${receipt.orderId}`)}</strong></p>
    <p>Date: ${receipt.orderDate ? new Date(receipt.orderDate).toLocaleString() : '—'}</p>
    <p>Cashier: ${escapeHtml(receipt.cashierName ?? '—')}</p>
    ${receipt.customerName ? `<p>Customer: ${escapeHtml(receipt.customerName)}</p>` : ''}
    ${payment ? `<p>Payment: ${escapeHtml(payment)}</p>` : ''}
    <table>${itemsHtml}</table>
    <div class="totals">
      ${receipt.subtotal != null ? `<div><span>Subtotal</span><span>${money(receipt.subtotal)}</span></div>` : ''}
      ${receipt.totalDiscount != null && receipt.totalDiscount > 0 ? `<div><span>Discount</span><span>-${money(receipt.totalDiscount)}</span></div>` : ''}
      ${receipt.taxRate != null && receipt.taxRate > 0 ? `<div><span>Tax (${receipt.taxRate}%)</span><span>${money(receipt.taxAmount)}</span></div>` : ''}
      <div class="grand"><span>Total</span><span>${money(receiptTotal(receipt))}</span></div>
      ${options?.tendered != null ? `<div><span>Tendered</span><span>${money(options.tendered)}</span></div><div><span>Change</span><span>${money(options.change ?? 0)}</span></div>` : ''}
    </div>
  </div>
  <script>window.onload = function () { window.print(); }</script>
</body>
</html>`

  const win = window.open('', '_blank', 'noopener,noreferrer,width=480,height=720')
  if (!win) {
    window.alert('Pop-up blocked — allow pop-ups to print the receipt.')
    return
  }
  win.document.open()
  win.document.write(html)
  win.document.close()
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}
